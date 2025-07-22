package com.helperlib.command;

import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.CommandType;
import com.helperlib.command.terminal.TerminalCommandFactory;
import com.helperlib.command.terminal.TerminalCommandMetadata;
import com.helperlib.core.command.CommandRegistry;
import com.helperlib.core.command.logging.FileStreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalCommandTest {

    private static final String TEST_CATEGORY = "TestCategory";
    private static final String TEST_GROUP = "TerminalTestGroup";
    private static final String TEST_COMMAND_NAME = "EchoHelloWorld";
    private static final String ECHO_COMMAND = "echo Hello, world!";
    private static final String EXPECTED_OUTPUT = "Hello, world!";

    @BeforeEach
    void setUp() {
        // Set the test environment property
        System.setProperty("IS_TEST", "true");

        // Register only the terminal command factory
        CommandRegistry.registerFactory(CommandType.TERMINAL, new TerminalCommandFactory());

        // Initialize config service
        CommandRegistry.getConfigService().initializeConfigFile();

        // Create and save the test command
        TerminalCommandMetadata testCommandMetadata = new TerminalCommandMetadata(
                TEST_COMMAND_NAME,
                "Test command to echo Hello, world!",
                ECHO_COMMAND,
                Map.of("LANG", "en_US.UTF-8"), // Environment variables
                "", // Use default working directory
                "" // Use default PATH
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, testCommandMetadata);
    }

    @Test
    void testTerminalCommand_happyFlow() throws IOException, InterruptedException {
        System.out.println("Testing terminal command happy flow...");

        // Create FileStreamHandler with the logs directory
        FileStreamHandler fileStreamHandler = new FileStreamHandler();

        // Execute the command using CommandRegistry with FileStreamHandler
        CompletableFuture<CommandResult> resultFuture = CommandRegistry.executeCommandFromConfig(
                TEST_CATEGORY,
                TEST_GROUP,
                TEST_COMMAND_NAME,
                fileStreamHandler
        );

        // Wait for command completion
        CommandResult result = resultFuture.join();

        // Verify command execution was successful
        assertNotNull(result, "Command result should not be null");
        assertTrue(result.success(), "Command execution should be successful");
        assertEquals(0, result.exitCode(), "Exit code should be 0 for successful execution");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        // Small delay to ensure file writing is complete
        Thread.sleep(200);

        // Verify that the output was written to the correct log file
        Path expectedLogPath = getExpectedLogPath(TEST_COMMAND_NAME, "stdout");
        assertTrue(Files.exists(expectedLogPath),
                "stdout log file should exist at: " + expectedLogPath);

        // Read and verify the content of the log file
        String logContent = Files.readString(expectedLogPath).trim();
        assertEquals(EXPECTED_OUTPUT, logContent,
                "Log file should contain the expected output: " + EXPECTED_OUTPUT);

        System.out.println("✓ Successfully verified that '" + EXPECTED_OUTPUT +
                "' was written to log file: " + expectedLogPath);
    }

    /**
     * Helper method to get the expected log file path based on FileStreamHandler logic
     */
    private Path getExpectedLogPath(String commandName, String streamType) {
        // Get the root path from CommandRegistry
        Path rootPath = Paths.get(CommandRegistry.getConfigService().getConfigFilePath()).getParent();
        Path logsDirectory = rootPath.resolve("logs");

        String sanitizedCommandName = commandName.replaceAll("[^a-zA-Z0-9.-]", "_");
        String fileName = String.format("%s_%s.log", sanitizedCommandName, streamType);
        return logsDirectory.resolve(fileName);
    }

}
