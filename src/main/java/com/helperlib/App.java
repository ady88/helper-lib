package com.helperlib;

import com.helperlib.command.*;
import java.util.Optional;

public class App {

    private final ConfigService configService;

    public App() {
        this.configService = new ConfigService();
        this.configService.initializeConfigFile();
    }

    /**
     * Executes a Command object using its metadata.
     */
    public void executeCommand(Command command) {
        if (command != null) {
            command.execute();
        } else {
            System.out.println("Command is null. Cannot execute.");
        }
    }

    /**
     * Resolves a Command by its name from the configuration and executes it.
     */
    public void executeCommandFromConfig(String commandName) {
        Optional<CommandMetadata> commandMetadataOpt = configService.getCommandMetadataByName(commandName);

        if (commandMetadataOpt.isPresent()) {
            CommandMetadata metadata = commandMetadataOpt.get();
            Command command = createCommandFromMetadata(metadata);
            executeCommand(command);
        } else {
            System.out.println("Command not found in configuration: " + commandName);
        }
    }

    /**
     * Creates a Command instance based on the specific CommandMetadata type.
     */
    private Command createCommandFromMetadata(CommandMetadata metadata) {
        return switch (metadata) {
            case ClipboardCommandMetadata clipboardMetadata ->
                    new ClipboardCommand(clipboardMetadata);
            case TerminalCommandMetadata terminalMetadata ->
                    new TerminalCommand(terminalMetadata);
            default -> throw new IllegalArgumentException(
                    "Unsupported CommandMetadata type: " + metadata.getClass().getSimpleName()
            );
        };
    }

    /**
     * Demonstrates copying text directly using the Command system.
     */
    public void copyTextToClipboard(String text) {
        // Create metadata
        ClipboardCommandMetadata metadata = new ClipboardCommandMetadata(
                "CopyToClipboard",
                "Command to copy text to clipboard",
                text
        );

        // Create and execute the command
        Command clipboardCommand = new ClipboardCommand(metadata);
        executeCommand(clipboardCommand);
    }

    /*
        For example, Adding a sample terminal command from config:
        You could invoke executeCommandFromConfig("SampleTerminalCommand")
        assuming the resolved metadata exists in the configuration file structured as:

        TerminalCommandMetadata example:
        {
            "name": "SampleTerminalCommand",
            "description": "A command test for terminal execution",
            "type": "TERMINAL",
            "commandText": "echo",
            "arguments": {"ENV_VAR": "value"},
            "path": "/path/to/directory"
        }
     */


}
