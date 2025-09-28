package com.helperlib.command.tunneltoggle;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.ToggleCommand;
import com.helperlib.core.command.CommandExecutorService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TunnelToggleCommand extends Command implements ToggleCommand {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isToggling = new AtomicBoolean(false);

    private final AtomicReference<Session> sessionRef = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<CommandResult>> currentExecution = new AtomicReference<>();
    private final AtomicReference<CountDownLatch> closeLatchRef = new AtomicReference<>();

    public TunnelToggleCommand(TunnelToggleCommandMetadata metadata) {
        super(metadata);
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        if (isRunning.get()) {
            return CompletableFuture.completedFuture(new CommandResult(false, -1, 0));
        }

        CompletableFuture<CommandResult> f = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            if (!isRunning.compareAndSet(false, true)) {
                return new CommandResult(false, -1, 0);
            }

            TunnelToggleCommandMetadata m = (TunnelToggleCommandMetadata) metadata;

            try {
                Session session = createAndConnectSession(m);
                sessionRef.set(session);

                // Setup local port forwarding (supports optional bind host)
                if (m.getLocalBindHost() != null && !m.getLocalBindHost().isBlank()) {
                    session.setPortForwardingL(m.getLocalBindHost(), m.getLocalPort(), m.getRemoteHost(), m.getRemotePort());
                } else {
                    session.setPortForwardingL(m.getLocalPort(), m.getRemoteHost(), m.getRemotePort());
                }

                System.out.printf("Tunnel established: %s:%d -> %s:%d via %s@%s:%d%n",
                        m.getLocalBindHost(), m.getLocalPort(), m.getRemoteHost(), m.getRemotePort(),
                        m.getUsername(), m.getHost(), m.getPort());

                // Wait until closed (toggleAsync will signal)
                CountDownLatch latch = new CountDownLatch(1);
                closeLatchRef.set(latch);

                // Periodically check connection; wake up on toggle
                while (session.isConnected()) {
                    if (latch.await(1, TimeUnit.SECONDS)) break;
                }

                long execTime = System.currentTimeMillis() - start;
                return new CommandResult(true, 0, execTime);

            } catch (Exception e) {
                long execTime = System.currentTimeMillis() - start;
                System.err.println("Tunnel start failed: " + e.getMessage());
                return new CommandResult(false, -1, execTime);

            } finally {
                try {
                    Session s = sessionRef.getAndSet(null);
                    if (s != null && s.isConnected()) {
                        // Best-effort cleanup (remove forwarding; disconnect)
                        tryRemoveForwarding(s, (TunnelToggleCommandMetadata) metadata);
                        s.disconnect();
                    }
                } catch (Exception ignored) {
                }
                CountDownLatch latch = closeLatchRef.getAndSet(null);
                if (latch != null) latch.countDown();
                isRunning.set(false);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());

        currentExecution.set(f);
        return f;
    }

    @Override
    public CompletableFuture<CommandResult> toggleAsync() {
        if (isToggling.get()) {
            return CompletableFuture.completedFuture(new CommandResult(false, -1, 0));
        }

        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            if (!isToggling.compareAndSet(false, true)) {
                return new CommandResult(false, -1, 0);
            }

            try {
                Session s = sessionRef.get();
                if (s == null || !s.isConnected()) {
                    // Nothing to stop
                    return new CommandResult(false, -1, System.currentTimeMillis() - start);
                }

                TunnelToggleCommandMetadata m = (TunnelToggleCommandMetadata) metadata;

                // Try graceful removal of port forwarding
                tryRemoveForwarding(s, m);

                // Disconnect session
                s.disconnect();

                // Signal waiter
                CountDownLatch latch = closeLatchRef.get();
                if (latch != null) latch.countDown();

                long execTime = System.currentTimeMillis() - start;
                // 130 as a conventional "interrupted/terminated" code
                return new CommandResult(true, 130, execTime);

            } catch (Exception e) {
                long execTime = System.currentTimeMillis() - start;
                System.err.println("Tunnel stop failed: " + e.getMessage());
                return new CommandResult(false, -1, execTime);

            } finally {
                isToggling.set(false);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }

    @Override
    public boolean isRunning() {
        Session s = sessionRef.get();
        return isRunning.get() && s != null && s.isConnected();
    }

    private Session createAndConnectSession(TunnelToggleCommandMetadata m) throws JSchException {
        Objects.requireNonNull(m.getHost(), "host");
        Objects.requireNonNull(m.getUsername(), "username");

        JSch jsch = new JSch();

        if (m.getKnownHostsPath() != null && !m.getKnownHostsPath().isBlank()) {
            jsch.setKnownHosts(m.getKnownHostsPath());
        }

        if (m.getAuthType() == TunnelAuthType.PRIVATE_KEY) {
            if (m.getPrivateKeyPath() == null || m.getPrivateKeyPath().isBlank()) {
                throw new IllegalArgumentException("privateKeyPath is required for PRIVATE_KEY auth");
            }
            if (m.getPassphrase() != null && !m.getPassphrase().isEmpty()) {
                jsch.addIdentity(m.getPrivateKeyPath(), m.getPassphrase());
            } else {
                jsch.addIdentity(m.getPrivateKeyPath());
            }
        }

        Session session = jsch.getSession(m.getUsername(), m.getHost(), m.getPort() > 0 ? m.getPort() : 22);

        if (m.getAuthType() == TunnelAuthType.PASSWORD) {
            if (m.getPassword() == null) {
                throw new IllegalArgumentException("password is required for PASSWORD auth");
            }
            session.setPassword(m.getPassword());
        }

        // Host key checking
        java.util.Properties cfg = new java.util.Properties();
        cfg.put("StrictHostKeyChecking", m.isStrictHostKeyChecking() ? "yes" : "no");
        session.setConfig(cfg);

        // Keepalive and connect
        if (m.getKeepAliveIntervalSec() > 0) {
            try {
                session.setServerAliveInterval(m.getKeepAliveIntervalSec() * 1000);
            } catch (Exception ignored) {
            }
        }

        session.connect(Math.max(1000, m.getConnectTimeoutMs() > 0 ? m.getConnectTimeoutMs() : 10_000));
        return session;
    }

    private void tryRemoveForwarding(Session session, TunnelToggleCommandMetadata m) {
        try {
            if (m.getLocalBindHost() != null && !m.getLocalBindHost().isBlank()) {
                session.delPortForwardingL(m.getLocalBindHost(), m.getLocalPort());
            } else {
                session.delPortForwardingL(m.getLocalPort());
            }
        } catch (Exception ignored) {
            // Ignore if not set or already removed
        }
    }
}
