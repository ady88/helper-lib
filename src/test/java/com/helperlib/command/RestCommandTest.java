
package com.helperlib.command;

import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.CommandType;
import com.helperlib.command.rest.RestCommand;
import com.helperlib.command.rest.RestCommandFactory;
import com.helperlib.command.rest.RestCommandMetadata;
import com.helperlib.core.command.CommandRegistry;
import com.helperlib.core.command.logging.FileStreamHandler;
import com.helperlib.core.command.logging.NoOpStreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class RestCommandTest {
    private static final String TEST_CATEGORY = "TestCategory";
    private static final String TEST_GROUP = "RestTestGroup";
    private static final String TEST_COMMAND_NAME = "TestApiCall";

    private static final String JSON_RESPONSE = """
            {
                "data": {
                    "user": {
                        "id": 123,
                        "name": "John Doe",
                        "email": "john.doe@example.com"
                    },
                    "token": "abc123xyz"
                },
                "status": "success"
            }""";

    private ClientAndServer mockServer;
    private static final int MOCK_SERVER_PORT = 1080;
    private static final String MOCK_SERVER_URL = "http://localhost:" + MOCK_SERVER_PORT;

    @BeforeEach
    void setUp() {
        // Set the test environment property
        System.setProperty("IS_TEST", "true");

        // Start mock server
        mockServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);

        // Register the REST command factory
        CommandRegistry.registerFactory(CommandType.REST, new RestCommandFactory());

        // Initialize config service
        CommandRegistry.getConfigService().initializeConfigFile();

        setupMockServerExpectations();
    }

    private void setupMockServerExpectations() {
        // Setup GET request expectation
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/users/123")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JSON_RESPONSE)
                );

        // Setup POST request expectation
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/api/users")
                                .withHeader("Authorization", "Bearer test-token")
                                .withBody("{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}")
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"id\":124,\"name\":\"Jane Doe\",\"email\":\"jane@example.com\",\"created\":true}")
                );

        // Setup PUT request expectation
        mockServer
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/api/users/123")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"id\":123,\"name\":\"John Updated\",\"updated\":true}")
                );

        // Setup DELETE request expectation
        mockServer
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/api/users/123")
                )
                .respond(
                        response()
                                .withStatusCode(204)
                );

        // Setup error response expectation
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/error")
                )
                .respond(
                        response()
                                .withStatusCode(500)
                                .withBody("Internal Server Error")
                );
    }

    @Test
    void testRestCommand_GET_happyFlow() throws InterruptedException, IOException, UnsupportedFlavorException {
        System.out.println("Testing REST GET command happy flow...");

        // Skip test if running in headless environment (CI/CD)
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        assumeTrue(!isHeadless, "Skipping clipboard test in headless environment (CI/CD)");

        // Create REST command metadata for GET request
        RestCommandMetadata getCommandMetadata = new RestCommandMetadata(
                TEST_COMMAND_NAME,
                "Test GET request to fetch user data",
                MOCK_SERVER_URL + "/api/users/123",
                "GET",
                null, // No request body for GET
                Map.of("Accept", "application/json"),
                null // Copy full response to clipboard
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, getCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                TEST_COMMAND_NAME,
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(200, result.exitCode(), "HTTP status code should be 200");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        // Small delay to ensure the clipboard operation is complete
        Thread.sleep(100);

        // Verify clipboard content contains the JSON response
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        assertTrue(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor),
                "Clipboard should contain string data");

        String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
        assertTrue(clipboardContent.contains("John Doe"),
                "Clipboard should contain the JSON response");
        assertTrue(clipboardContent.contains("john.doe@example.com"),
                "Clipboard should contain the user email");

        System.out.println("✓ Successfully verified REST GET command execution");
    }

    @Test
    void testRestCommand_POST_withBody() throws InterruptedException {
        System.out.println("Testing REST POST command with request body...");

        String postCommandName = "CreateUser";
        RestCommandMetadata postCommandMetadata = new RestCommandMetadata(
                postCommandName,
                "Test POST request to create user",
                MOCK_SERVER_URL + "/api/users",
                "POST",
                "{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}",
                Map.of(
                        "Content-Type", "application/json",
                        "Authorization", "Bearer test-token"
                ),
                null
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, postCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                postCommandName,
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(201, result.exitCode(), "HTTP status code should be 201 (Created)");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        System.out.println("✓ Successfully verified REST POST command execution");
    }

    @Test
    void testRestCommand_jsonPathExtraction() throws InterruptedException, IOException, UnsupportedFlavorException {
        System.out.println("Testing REST command with JSON path extraction...");

        // Skip test if running in headless environment (CI/CD)
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        assumeTrue(!isHeadless, "Skipping clipboard test in headless environment (CI/CD)");

        String jsonPathCommandName = "GetUserEmail";
        RestCommandMetadata jsonPathCommandMetadata = new RestCommandMetadata(
                jsonPathCommandName,
                "Test GET request with JSON path extraction",
                MOCK_SERVER_URL + "/api/users/123",
                "GET",
                null,
                Map.of("Accept", "application/json"),
                "data.user.email" // Extract only the email field
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, jsonPathCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                jsonPathCommandName,
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(200, result.exitCode(), "HTTP status code should be 200");

        // Small delay to ensure the clipboard operation is complete
        Thread.sleep(100);

        // Verify clipboard content contains only the extracted email
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        assertTrue(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor),
                "Clipboard should contain string data");

        String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
        assertEquals("john.doe@example.com", clipboardContent,
                "Clipboard should contain only the extracted email");

        System.out.println("✓ Successfully verified REST command JSON path extraction");
    }

    @Test
    void testRestCommand_PUT_request() throws InterruptedException {
        System.out.println("Testing REST PUT command...");

        String putCommandName = "UpdateUser";
        RestCommandMetadata putCommandMetadata = new RestCommandMetadata(
                putCommandName,
                "Test PUT request to update user",
                MOCK_SERVER_URL + "/api/users/123",
                "PUT",
                "{\"name\":\"John Updated\"}",
                Map.of("Content-Type", "application/json"),
                null
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, putCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                putCommandName,
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(200, result.exitCode(), "HTTP status code should be 200");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        System.out.println("✓ Successfully verified REST PUT command execution");
    }

    @Test
    void testRestCommand_DELETE_request() throws InterruptedException {
        System.out.println("Testing REST DELETE command...");

        String deleteCommandName = "DeleteUser";
        RestCommandMetadata deleteCommandMetadata = new RestCommandMetadata(
                deleteCommandName,
                "Test DELETE request to remove user",
                MOCK_SERVER_URL + "/api/users/123",
                "DELETE",
                null,
                null,
                null
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, deleteCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                deleteCommandName,
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(204, result.exitCode(), "HTTP status code should be 204 (No Content)");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        System.out.println("✓ Successfully verified REST DELETE command execution");
    }

    @Test
    void testRestCommand_errorResponse() throws InterruptedException {
        System.out.println("Testing REST command error handling...");

        String errorCommandName = "ErrorRequest";
        RestCommandMetadata errorCommandMetadata = new RestCommandMetadata(
                errorCommandName,
                "Test request that returns error",
                MOCK_SERVER_URL + "/api/error",
                "GET",
                null,
                null,
                null
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, errorCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                errorCommandName,
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution failed due to server error
        assertNotNull(result, "Command result should not be null");
        assertFalse(result.success(), "Command execution should fail for 5xx status codes");
        assertEquals(500, result.exitCode(), "HTTP status code should be 500");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        System.out.println("✓ Successfully verified REST command error handling");
    }

    @Test
    void testRestCommand_withFileStreamHandler() throws InterruptedException, IOException {
        System.out.println("Testing REST command with FileStreamHandler...");

        String streamCommandName = "StreamedRequest";
        RestCommandMetadata streamCommandMetadata = new RestCommandMetadata(
                streamCommandName,
                "Test request with file stream logging",
                MOCK_SERVER_URL + "/api/users/123",
                "GET",
                null,
                Map.of("Accept", "application/json"),
                null
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, streamCommandMetadata);

        FileStreamHandler fileStreamHandler = new FileStreamHandler();

        // Execute the command using CommandRegistry with FileStreamHandler
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                streamCommandName,
                fileStreamHandler
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(200, result.exitCode(), "HTTP status code should be 200");

        // Small delay to ensure file writing is complete
        Thread.sleep(200);

        // Note: FileStreamHandler creates log files, but we don't verify them here
        // as the exact file path depends on the implementation
        System.out.println("✓ Successfully verified REST command with FileStreamHandler");
    }

    @Test
    void testRestCommand_invalidJsonPath() throws InterruptedException, IOException, UnsupportedFlavorException {
        System.out.println("Testing REST command with invalid JSON path...");

        // Skip test if running in headless environment (CI/CD)
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        assumeTrue(!isHeadless, "Skipping clipboard test in headless environment (CI/CD)");

        String invalidPathCommandName = "InvalidJsonPath";
        RestCommandMetadata invalidPathCommandMetadata = new RestCommandMetadata(
                invalidPathCommandName,
                "Test request with invalid JSON path",
                MOCK_SERVER_URL + "/api/users/123",
                "GET",
                null,
                Map.of("Accept", "application/json"),
                "nonexistent.field" // This path doesn't exist in the response
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, invalidPathCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                invalidPathCommandName,
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful (HTTP call succeeded)
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(200, result.exitCode(), "HTTP status code should be 200");

        // Small delay to ensure the clipboard operation is complete
        Thread.sleep(100);

        // Verify clipboard content contains full response (fallback behavior)
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        assertTrue(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor),
                "Clipboard should contain string data");

        String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
        assertTrue(clipboardContent.contains("John Doe"),
                "Clipboard should contain full response when JSON path extraction fails");

        System.out.println("✓ Successfully verified REST command fallback behavior for invalid JSON path");
    }

    @Test
    void testRestCommand_headlessEnvironment() throws InterruptedException {
        System.out.println("Testing REST command in headless environment...");

        // This test specifically runs in headless environments
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        if (!isHeadless) {
            System.out.println("Skipping headless test - running in graphical environment");
            return;
        }

        RestCommandMetadata headlessCommandMetadata = new RestCommandMetadata(
                "HeadlessRequest",
                "Test request in headless environment",
                MOCK_SERVER_URL + "/api/users/123",
                "GET",
                null,
                null,
                null
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, headlessCommandMetadata);

        // Execute the command using CommandRegistry
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                "HeadlessRequest",
                new NoOpStreamHandler()
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful (clipboard failure is handled gracefully)
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful even in headless environment");
        assertEquals(200, result.exitCode(), "HTTP status code should be 200");

        System.out.println("✓ Successfully verified that REST command works in headless environment");
    }

    @Test
    void testRestCommand_directExecution() throws InterruptedException {
        System.out.println("Testing direct REST command execution...");

        RestCommandMetadata directCommandMetadata = new RestCommandMetadata(
                "DirectExecution",
                "Test direct command execution",
                MOCK_SERVER_URL + "/api/users/123",
                "GET",
                null,
                Map.of("Accept", "application/json"),
                "data.token"
        );

        // Create command directly (not via CommandRegistry)
        RestCommand restCommand = new RestCommand(directCommandMetadata, new NoOpStreamHandler());

        // Execute command directly
        CompletableFuture<CommandResult> resultFuture = restCommand.executeAsync();
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(200, result.exitCode(), "HTTP status code should be 200");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        System.out.println("✓ Successfully verified direct REST command execution");
    }

    @Test
    void testRestCommandMetadata_serialization() {
        System.out.println("Testing REST command metadata serialization...");

        RestCommandFactory factory = new RestCommandFactory();

        RestCommandMetadata originalMetadata = new RestCommandMetadata(
                "SerializationTest",
                "Test serialization",
                "https://api.example.com/test",
                "POST",
                "{\"test\":\"data\"}",
                Map.of("Authorization", "Bearer token", "Content-Type", "application/json"),
                "result.id"
        );

        // Serialize metadata
        var jsonObject = factory.serializeMetadata(originalMetadata);

        // Deserialize metadata
        var deserializedMetadata = (RestCommandMetadata) factory.parseMetadata(jsonObject);

        // Verify all fields are preserved
        assertEquals(originalMetadata.getName(), deserializedMetadata.getName());
        assertEquals(originalMetadata.getDescription(), deserializedMetadata.getDescription());
        assertEquals(originalMetadata.getUrl(), deserializedMetadata.getUrl());
        assertEquals(originalMetadata.getMethod(), deserializedMetadata.getMethod());
        assertEquals(originalMetadata.getRequestBody(), deserializedMetadata.getRequestBody());
        assertEquals(originalMetadata.getToClipboard(), deserializedMetadata.getToClipboard());
        assertEquals(originalMetadata.getHeaders(), deserializedMetadata.getHeaders());

        System.out.println("✓ Successfully verified REST command metadata serialization");
    }

    // Cleanup method to stop mock server after each test
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
}