package com.helperlib.command;

import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.CommandType;
import com.helperlib.command.clipboard.ClipboardCommandFactory;
import com.helperlib.command.clipboard.ClipboardCommandMetadata;
import com.helperlib.core.command.CommandRegistry;
import com.helperlib.core.command.logging.NoOpStreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class ClipboardCommandTest {
    private static final String TEST_CATEGORY = "TestCategory";
    private static final String TEST_GROUP = "ClipboardTestGroup";
    private static final String TEST_COMMAND_NAME = "HelloWorldCopy";
    private static final String TEST_TEXT = "Hello,world!";

    @BeforeEach
    void setUp() {
        // Set the test environment property
        System.setProperty("IS_TEST", "true");

        // Register only the clipboard command factory (no need for terminal)
        CommandRegistry.registerFactory(CommandType.CLIPBOARD, new ClipboardCommandFactory());

        // Initialize config service
        CommandRegistry.getConfigService().initializeConfigFile();

        // Create and save the test command
        ClipboardCommandMetadata testCommandMetadata = new ClipboardCommandMetadata(
                TEST_COMMAND_NAME,
                "Test command to copy Hello,world! to clipboard",
                TEST_TEXT
        );

        CommandRegistry.saveCommandToConfig(TEST_CATEGORY, TEST_GROUP, testCommandMetadata);
    }

    @Test
    void testClipboardCommand_happyFlow() throws IOException, UnsupportedFlavorException, InterruptedException {
        System.out.println("Testing clipboard command happy flow...");

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
        assertEquals(0, result.exitCode(), "Exit code should be 0 for successful execution");
        assertTrue(result.executionTimeMs() >= 0, "Execution time should be non-negative");

        // Small delay to ensure the clipboard operation is complete
        Thread.sleep(100);

        // Verify clipboard content
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        assertTrue(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor),
                "Clipboard should contain string data");

        String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
        assertEquals(TEST_TEXT, clipboardContent,
                "Clipboard should contain the expected text: " + TEST_TEXT);

        System.out.println("✓ Successfully verified that '" + TEST_TEXT + "' was copied to clipboard");
    }
}
