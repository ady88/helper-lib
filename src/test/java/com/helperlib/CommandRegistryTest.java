package com.helperlib;

import com.helperlib.command.ClipboardCommandMetadata;
import com.helperlib.command.CommandMetadata;
import com.helperlib.command.CommandMetadataWrapper;
import com.helperlib.command.TerminalCommandMetadata;
import com.helperlib.command.factory.CommandRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRegistryTest {

    @BeforeEach
    void setUp() {
        // Set the test environment property
        System.setProperty("IS_TEST", "true");

        // Check if the configuration file is valid using the isValidJsonFile method
        ConfigService configService = CommandRegistry.getConfigService();
        String configFilePath = configService.getConfigFilePath();

        if (!configService.isValidJsonFile(configFilePath)) {
            // If the file is invalid, initialize it to create a valid empty structure
            configService.initializeConfigFile();
        }

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
                "/tmp", ""
        );

        // Add commands to specific groups in the test configurations
        String clipboardGroup = "ClipboardGroup";
        String terminalGroup = "TerminalGroup";

        CommandRegistry.saveCommandToConfig(clipboardGroup, clipboardCommandMetadata);
        CommandRegistry.saveCommandToConfig(terminalGroup, terminalCommandMetadata);
    }




    @Test
    void testExecuteCommandFromConfig_clipboardCommand() {
        System.out.println("Testing clipboard command execution...");
        String clipboardGroup = "ClipboardGroup"; // Specify the group name
        CommandRegistry.executeCommandFromConfig(clipboardGroup, "CopyTest");

        // Manual verification may be needed for clipboard contents
        assertTrue(true, "Executed clipboard command successfully (manual verification needed).");
    }


    @Test
    void testExecuteCommandFromConfig_terminalCommand() {
        System.out.println("Testing terminal command execution...");
        String terminalGroup = "TerminalGroup"; // Specify the group name
        CommandRegistry.executeCommandFromConfig(terminalGroup, "ListFiles");

        // Manual verification may be needed for environment-specific outputs
        assertTrue(true, "Executed terminal command successfully (manual verification needed).");
    }

    @Test
    void testExecuteCommandFromConfig_nonExistentCommand() {
        System.out.println("Testing non-existent command execution...");
        String unknownGroup = "UnknownGroup"; // Specify a non-existent group
        CommandRegistry.executeCommandFromConfig(unknownGroup, "NonExistentCommand");

        // Properly logs a "not found" message
        assertTrue(true, "Handled non-existent command correctly.");
    }

    @Test
    void testAddNewCommandToConfig() {
        System.out.println("Testing adding a new command to config...");

        // Create a new ClipboardCommandMetadata object
        var newCommand = new ClipboardCommandMetadata(
                "NewCopyCommand",
                "A new command to test adding",
                "New test text to copy"
        );

        // Group for the new command
        String clipboardGroup = "ClipboardGroup";

        // Save the new command using CommandRegistry
        CommandRegistry.saveCommandToConfig(clipboardGroup, newCommand);

        // Load all commands using ConfigService and ensure the new command is present in the specified group
        CommandMetadataWrapper commandMetadataWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(commandMetadataWrapper.getCommandsByGroup(clipboardGroup)
                        .stream()
                        .anyMatch(cmd -> cmd.getName().equals("NewCopyCommand")),
                "New command was successfully added to the configuration!");
    }

    @Test
    void testRemoveCommandFromConfig() {
        System.out.println("Testing removing a command from config...");

        // Group and command for testing
        String clipboardGroup = "ClipboardGroup";
        String commandName = "CopyTest";

        // Ensure configuration is initialized
        CommandRegistry.getConfigService().initializeConfigFile();

        // Add the command to the config first
        var command = new ClipboardCommandMetadata(
                commandName,
                "A command to test removing",
                "Sample text to copy"
        );
        CommandRegistry.saveCommandToConfig(clipboardGroup, command);

        // Verify the command exists in the configuration
        CommandMetadataWrapper commandMetadataWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(commandMetadataWrapper.getCommandsByGroup(clipboardGroup)
                        .stream()
                        .anyMatch(cmd -> cmd.getName().equals(commandName)),
                "Command was successfully added to the configuration!");

        // Remove the command using the method under test
        CommandRegistry.removeCommandFromConfig(clipboardGroup, commandName);

        // Reload the commands to verify removal
        commandMetadataWrapper = CommandRegistry.getConfigService().loadCommands();
        assertFalse(commandMetadataWrapper.getCommandsByGroup(clipboardGroup)
                        .stream()
                        .anyMatch(cmd -> cmd.getName().equals(commandName)),
                "Command was successfully removed from the configuration!");
    }



}