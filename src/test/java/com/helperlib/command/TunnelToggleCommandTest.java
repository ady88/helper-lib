package com.helperlib.command;

import com.helperlib.api.command.CommandResult;
import com.helperlib.command.tunneltoggle.TunnelAuthType;
import com.helperlib.command.tunneltoggle.TunnelToggleCommand;
import com.helperlib.command.tunneltoggle.TunnelToggleCommandMetadata;

import com.sun.net.httpserver.HttpServer;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.bouncycastle.openssl.PEMWriter;


import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class TunnelToggleCommandTest {

    private SshServer sshd;
    private HttpServer backendHttpServer;
    private int backendPort;
    private int sshPort;

    @BeforeEach
    void setUp() throws Exception {
        System.setProperty("IS_TEST", "true");

        // Start tiny HTTP backend to verify tunnel forwarding
        backendPort = findFreePort();
        backendHttpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", backendPort), 0);
        backendHttpServer.createContext("/ping", exchange -> {
            byte[] body = "pong".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        backendHttpServer.setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor());
        backendHttpServer.start();

        // Start in-memory SSH server that allows password auth and local port forwarding
        sshd = SshServer.setUpDefaultServer();
        sshd.setHost("127.0.0.1");
        sshd.setPort(0); // random free port

        var hostKeyPath = Files.createTempFile("hostkey", ".ser");
        var hostKeyProvider = new SimpleGeneratorHostKeyProvider(hostKeyPath);
        // Optional: choose algorithm explicitly (RSA is broadly compatible)
        hostKeyProvider.setAlgorithm("RSA");
        sshd.setKeyPairProvider(hostKeyProvider);

        sshd.setPasswordAuthenticator((username, password, session) ->
                "testuser".equals(username) && "testpass".equals(password));
        sshd.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
        sshd.start();
        sshPort = sshd.getPort();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (backendHttpServer != null) {
            backendHttpServer.stop(0);
        }
        if (sshd != null) {
            try {
                sshd.stop(true);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    void testTunnelToggleCommand_happyFlow_startAndStop() throws Exception {
        int localPort = findFreePort();

        TunnelToggleCommandMetadata meta = new TunnelToggleCommandMetadata(
                "TestTunnel",
                "SSH tunnel test - happy flow",
                "127.0.0.1",
                sshPort,
                "testuser",
                TunnelAuthType.PASSWORD,
                "testpass",
                null,
                null,
                false,
                null,
                "127.0.0.1",
                localPort,
                "127.0.0.1",
                backendPort,
                5_000,
                5
        );

        TunnelToggleCommand command = new TunnelToggleCommand(meta);

        CompletableFuture<CommandResult> runFuture = command.executeAsync();

        // Wait until tunnel is up (up to ~3 seconds)
        waitUntilTrue(command::isRunning, 3000, "Tunnel did not start in time");

        // Verify traffic flows through the tunnel
        String url = "http://127.0.0.1:" + localPort + "/ping";
        String body = httpGet(url, Duration.ofSeconds(2));
        assertEquals("pong", body, "Expected backend response through the tunnel");

        // Stop tunnel via toggle
        CommandResult toggleResult = command.toggleAsync().join();
        assertNotNull(toggleResult);
        assertTrue(toggleResult.success(), "toggle should succeed");
        assertEquals(130, toggleResult.exitCode(), "toggle exit code should be 130");

        // The main future resolves after the session closes
        CommandResult runResult = runFuture.join();
        assertNotNull(runResult);
        assertTrue(runResult.success(), "run should conclude successfully after stop");

        // Ensure tunnel is no longer reachable
        Optional<String> afterStop = httpGetOptional(url, Duration.ofMillis(500));
        assertTrue(afterStop.isEmpty(), "Tunnel should be closed and not accept connections");
    }

    @Test
    void testTunnelToggleCommand_privateKeyAuth_success() throws Exception {
        int localPort = findFreePort();

        // Generate a test key pair for this test
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        // Start SSH server with public key auth
        sshd.setPublickeyAuthenticator((username, key, session) ->
                username.equals("testuser") && key.equals(keyPair.getPublic()));

        // Create a temporary private key file
        Path privateKeyPath = Files.createTempFile("test_private_key", "");
        try (PEMWriter pemWriter = new PEMWriter(new FileWriter(privateKeyPath.toFile()))) {
            pemWriter.writeObject(keyPair.getPrivate());
        }

        TunnelToggleCommandMetadata meta = new TunnelToggleCommandMetadata(
                "PrivateKeyTunnel",
                "SSH tunnel test - private key auth",
                "127.0.0.1",
                sshPort,
                "testuser",
                TunnelAuthType.PRIVATE_KEY,
                null, // no password
                privateKeyPath.toString(),
                null, // no passphrase
                false,
                null,
                "127.0.0.1",
                localPort,
                "127.0.0.1",
                backendPort,
                5_000,
                5
        );

        TunnelToggleCommand command = new TunnelToggleCommand(meta);

        CompletableFuture<CommandResult> runFuture = command.executeAsync();

        // Wait until tunnel is up (up to ~3 seconds)
        waitUntilTrue(command::isRunning, 3000, "Tunnel did not start in time");

        // Verify traffic flows through the tunnel
        String url = "http://127.0.0.1:" + localPort + "/ping";
        String body = httpGet(url, Duration.ofSeconds(2));
        assertEquals("pong", body, "Expected backend response through the tunnel");

        // Stop tunnel via toggle
        CommandResult toggleResult = command.toggleAsync().join();
        assertNotNull(toggleResult);
        assertTrue(toggleResult.success(), "toggle should succeed");
        assertEquals(130, toggleResult.exitCode(), "toggle exit code should be 130");

        // The main future resolves after the session closes
        CommandResult runResult = runFuture.join();
        assertNotNull(runResult);
        assertTrue(runResult.success(), "run should conclude successfully after stop");

        // Ensure tunnel is no longer reachable
        Optional<String> afterStop = httpGetOptional(url, Duration.ofMillis(500));
        assertTrue(afterStop.isEmpty(), "Tunnel should be closed and not accept connections");

        // Clean up
        Files.deleteIfExists(privateKeyPath);
    }


    @Test
    void testTunnelToggleCommand_negative_invalidCredentials() {
        int localPort = findFreePort();

        TunnelToggleCommandMetadata meta = new TunnelToggleCommandMetadata(
                "BadCredentialsTunnel",
                "SSH tunnel test - invalid credentials",
                "127.0.0.1",
                sshPort,
                "testuser",
                TunnelAuthType.PASSWORD,
                "wrongpass",
                null,
                null,
                false,
                null,
                "127.0.0.1",
                localPort,
                "127.0.0.1",
                backendPort,
                2_000,
                0
        );

        TunnelToggleCommand command = new TunnelToggleCommand(meta);
        CommandResult result = command.executeAsync().join();

        assertNotNull(result);
        assertFalse(result.success(), "Execution should fail for invalid credentials");
        assertEquals(-1, result.exitCode(), "Should return -1 on failure");
        assertFalse(command.isRunning(), "Tunnel should not be running");
    }

    // Helpers

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Unable to find a free port", e);
        }
    }

    private static void waitUntilTrue(BooleanSupplierThrowing cond, long timeoutMs, String message) throws Exception {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (cond.getAsBoolean()) {
                return;
            }
            Thread.sleep(50);
        }
        fail(message);
    }

    private static String httpGet(String url, Duration timeout) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(timeout).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    private static Optional<String> httpGetOptional(String url, Duration timeout) {
        try {
            return Optional.of(httpGet(url, timeout));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface BooleanSupplierThrowing {
        boolean getAsBoolean() throws Exception;
    }
}