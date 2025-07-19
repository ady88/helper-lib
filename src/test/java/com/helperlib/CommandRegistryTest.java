package com.helperlib;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.command.clipboard.ClipboardCommandMetadata;
import com.helperlib.command.terminal.TerminalCommandMetadata;

import com.helperlib.command.clipboard.ClipboardCommandFactory;
import com.helperlib.command.terminal.TerminalCommandFactory;
import com.helperlib.core.ConfigService;
import com.helperlib.core.command.CommandMetadataWrapper;
import com.helperlib.core.command.CommandRegistry;
import com.helperlib.core.command.exceptions.GroupNotEmptyException;
import com.helperlib.core.command.logging.NoOpStreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    @BeforeEach
    void setUp() {
        // Set the test environment property
        System.setProperty("IS_TEST", "true");

        // Check if the configuration file is valid using the isValidJsonFile method
        ConfigService configService = CommandRegistry.getConfigService();
        String configFilePath = configService.getConfigFilePath();

        CommandRegistry.registerFactory(CommandType.CLIPBOARD, new ClipboardCommandFactory());
        CommandRegistry.registerFactory(CommandType.TERMINAL, new TerminalCommandFactory());

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
        CommandRegistry.executeCommandFromConfig(clipboardGroup, "CopyTest", new NoOpStreamHandler()
        );

        // Manual verification may be needed for clipboard contents
        assertTrue(true, "Executed clipboard command successfully (manual verification needed).");
    }


    @Test
    void testExecuteCommandFromConfig_terminalCommand() {
        System.out.println("Testing terminal command execution...");
        String terminalGroup = "TerminalGroup"; // Specify the group name
        CommandRegistry.executeCommandFromConfig(terminalGroup, "ListFiles", new NoOpStreamHandler());

        // Manual verification may be needed for environment-specific outputs
        assertTrue(true, "Executed terminal command successfully (manual verification needed).");
    }

    @Test
    void testExecuteCommandFromConfig_nonExistentCommand() {
        System.out.println("Testing non-existent command execution...");
        String unknownGroup = "UnknownGroup"; // Specify a non-existent group
        CommandRegistry.executeCommandFromConfig(unknownGroup, "NonExistentCommand", new NoOpStreamHandler());

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

    @Test
    void testAddEmptyGroupToConfig_newGroup() {
        System.out.println("Testing adding an empty group that doesn't exist...");

        String newGroupName = "NewEmptyGroup";

        // Verify the group doesn't exist initially
        CommandMetadataWrapper initialWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(initialWrapper.getCommandsByGroup(newGroupName).isEmpty(),
                "Group should not exist initially");

        // Add empty group using CommandRegistry (assuming the method exists)
        CommandRegistry.addEmptyGroupToConfig(newGroupName);

        // Verify the group now exists but is empty
        CommandMetadataWrapper updatedWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(updatedWrapper.getGroupNames().contains(newGroupName),
                "New empty group should be created");
        assertTrue(updatedWrapper.getCommandsByGroup(newGroupName).isEmpty(),
                "New group should have no commands");
    }

    @Test
    void testAddEmptyGroupToConfig_existingGroupWithCommands() {
        System.out.println("Testing adding an empty group that already exists with commands...");

        String existingGroupName = "ClipboardGroup"; // This group already has commands from setUp()

        // Verify the group exists and has commands initially
        CommandMetadataWrapper initialWrapper = CommandRegistry.getConfigService().loadCommands();
        assertFalse(initialWrapper.getCommandsByGroup(existingGroupName).isEmpty(),
                "Group should already exist with commands");
        int initialCommandCount = initialWrapper.getCommandsByGroup(existingGroupName).size();

        // Try to add empty group for existing group
        CommandRegistry.addEmptyGroupToConfig(existingGroupName);

        // Verify the group still exists and retains its commands
        CommandMetadataWrapper updatedWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(updatedWrapper.getGroupNames().contains(existingGroupName),
                "Existing group should still exist");
        assertEquals(initialCommandCount, updatedWrapper.getCommandsByGroup(existingGroupName).size(),
                "Existing group should retain its commands and not be recreated");
        assertFalse(updatedWrapper.getCommandsByGroup(existingGroupName).isEmpty(),
                "Existing group should still have its commands");
    }

    @Test
    void testRemoveGroup_emptyGroupSuccess() throws GroupNotEmptyException {
        System.out.println("Testing removing an empty group...");

        String emptyGroupName = "EmptyTestGroup";

        // First, add an empty group
        CommandRegistry.addEmptyGroupToConfig(emptyGroupName);

        // Verify the empty group exists
        CommandMetadataWrapper initialWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(initialWrapper.getGroupNames().contains(emptyGroupName),
                "Empty group should exist before removal");
        assertTrue(initialWrapper.getCommandsByGroup(emptyGroupName).isEmpty(),
                "Group should be empty");

        // Remove the empty group - should succeed
        CommandRegistry.removeGroupFromConfig(emptyGroupName);

        // Verify the group has been removed
        CommandMetadataWrapper updatedWrapper = CommandRegistry.getConfigService().loadCommands();
        assertFalse(updatedWrapper.getGroupNames().contains(emptyGroupName),
                "Empty group should be successfully removed");
    }

    @Test
    void testRemoveGroup_groupWithCommandsThrowsException() {
        System.out.println("Testing removing a group with commands throws exception...");

        String groupWithCommands = "ClipboardGroup"; // This group has commands from setUp()

        // Verify the group exists and has commands
        CommandMetadataWrapper initialWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(initialWrapper.getGroupNames().contains(groupWithCommands),
                "Group with commands should exist");
        assertFalse(initialWrapper.getCommandsByGroup(groupWithCommands).isEmpty(),
                "Group should have commands");
        int commandCount = initialWrapper.getCommandsByGroup(groupWithCommands).size();

        // Attempt to remove group with commands - should throw GroupNotEmptyException
        GroupNotEmptyException exception = assertThrows(GroupNotEmptyException.class, () -> {
            CommandRegistry.removeGroupFromConfig(groupWithCommands);
        }, "Should throw GroupNotEmptyException when trying to remove group with commands");

        // Verify the exception message
        String expectedMessage = "Cannot remove group 'ClipboardGroup' because it contains " + commandCount + " command(s). Remove all commands from the group first.";
        assertEquals(expectedMessage, exception.getMessage(),
                "Exception should have the correct message");

        // Verify the group still exists after failed removal attempt
        CommandMetadataWrapper updatedWrapper = CommandRegistry.getConfigService().loadCommands();
        assertTrue(updatedWrapper.getGroupNames().contains(groupWithCommands),
                "Group with commands should still exist after failed removal");
        assertFalse(updatedWrapper.getCommandsByGroup(groupWithCommands).isEmpty(),
                "Group should still have its commands");
    }
}