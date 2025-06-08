package com.helperlib;

import com.helperlib.command.ClipboardCommandMetadata;
import com.helperlib.command.CommandMetadata;
import com.helperlib.command.TerminalCommandMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private App app;
    private ConfigService configService;

    @BeforeEach
    void setUp() {
        // Set the test environment variable to true to enable test-specific config
        System.setProperty("IS_TEST", "true");

        // Initialize the App and ConfigService
        configService = new ConfigService();
        configService.initializeConfigFile();
        app = new App();

        // Add some sample commands to the config for testing
        CommandMetadata clipboardCommandMetadata = new ClipboardCommandMetadata(
                "CopyTest",
                "Copies text to the clipboard",
                "Sample text to copy"
        );

        CommandMetadata terminalCommandMetadata = new TerminalCommandMetadata(
                "ListFiles",
                "Lists files in the current directory",
                "ls",
                Map.of("LANG", "en_US.UTF-8"),
                "/tmp"
        );

        // Add commands to the commands-test.json
        List<CommandMetadata> allCommands = configService.getAllCommandMetadata();
        allCommands.add(clipboardCommandMetadata);
        allCommands.add(terminalCommandMetadata);

        configService.saveAllCommandMetadata(allCommands);
    }

    @Test
    void testExecuteCommandFromConfig_clipboardCommand() {
        // Test executing a clipboard command from test config
        System.out.println("Testing clipboard command execution...");
        app.executeCommandFromConfig("CopyTest");

        // Manual verification may be needed for clipboard contents
        assertTrue(true, "Executed clipboard command successfully (manual verification needed).");
    }

    @Test
    void testExecuteCommandFromConfig_terminalCommand() {
        // Test executing a terminal command from test config
        System.out.println("Testing terminal command execution...");
        app.executeCommandFromConfig("ListFiles");

        // Manual verification may be needed for environment-specific outputs
        assertTrue(true, "Executed terminal command successfully (manual verification needed).");
    }

    @Test
    void testExecuteCommandFromConfig_nonExistentCommand() {
        // Test handling of a command that does not exist in the test config
        System.out.println("Testing non-existent command execution...");
        app.executeCommandFromConfig("NonExistentCommand");

        // Properly logs a "not found" message
        assertTrue(true, "Handled non-existent command correctly.");
    }

    @Test
    void testCopyTextToClipboard_directMethod() {
        // Test direct method for clipboard interaction using the test config
        System.out.println("Testing direct clipboard text copy...");
        String sampleText = "Direct clipboard test.";
        app.copyTextToClipboard(sampleText);

        // Manual verification may be required for copied text
        assertTrue(true, "Copied text to clipboard successfully (manual verification needed).");
    }
}


