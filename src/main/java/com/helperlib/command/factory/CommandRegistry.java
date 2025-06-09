package com.helperlib.command.factory;

import com.helperlib.ConfigService;
import com.helperlib.command.Command;
import com.helperlib.command.CommandMetadata;
import com.helperlib.command.CommandType;
import com.helperlib.command.CommandMetadataWrapper;
import jakarta.json.JsonObject;

import java.util.*;

public class CommandRegistry {
    private static final Map<CommandType, CommandFactory> factories = new HashMap<>();
    private static final ConfigService configService = new ConfigService(); // Reuse ConfigService

    static {
        // Register known command types
        registerFactory(CommandType.CLIPBOARD, new ClipboardCommandFactory());
        registerFactory(CommandType.TERMINAL, new TerminalCommandFactory());

        // Initialize the configuration file using ConfigService
        configService.initializeConfigFile();
    }

    /**
     * Registers a CommandFactory for a specific CommandType.
     */
    public static void registerFactory(CommandType type, CommandFactory factory) {
        factories.put(type, factory);
    }

    /**
     * Parses metadata from a JSON object using the appropriate CommandFactory.
     */
    public static CommandMetadata parseMetadata(JsonObject jsonObject) {
        CommandType type = CommandType.valueOf(jsonObject.getString("type"));
        CommandFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported command type: " + type);
        }
        return factory.parseMetadata(jsonObject);
    }

    /**
     * Serializes a CommandMetadata object into a JSON object using the appropriate CommandFactory.
     */
    public static JsonObject serializeMetadata(CommandMetadata metadata) {
        CommandFactory factory = factories.get(metadata.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported command type: " + metadata.getType());
        }
        return factory.serializeMetadata(metadata);
    }

    /**
     * Creates a Command instance from metadata using the appropriate CommandFactory.
     */
    public static Command createCommand(CommandMetadata metadata) {
        CommandFactory factory = factories.get(metadata.getType());
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported command type: " + metadata.getType());
        }
        return factory.createCommand(metadata);
    }

    /**
     * Resolves a command by its group and name from the ConfigService and executes it.
     *
     * @param group       The group to which the command belongs.
     * @param commandName The name of the command to execute.
     */
    public static void executeCommandFromConfig(String group, String commandName) {
        Optional<CommandMetadata> commandMetadataOpt = configService.getCommandByGroupAndName(group, commandName);

        if (commandMetadataOpt.isPresent()) {
            CommandMetadata metadata = commandMetadataOpt.get();
            Command command = createCommand(metadata);
            executeCommand(command);
        } else {
            System.out.println("Command not found in group '" + group + "' with name: " + commandName);
        }
    }


    /**
     * Executes a Command object.
     */
    public static void executeCommand(Command command) {
        if (command != null) {
            command.execute();
        } else {
            System.out.println("Command is null. Cannot execute.");
        }
    }

    /**
     * Adds or updates a command in the configuration file using ConfigService.
     *
     * @param group    The group to which the command belongs.
     * @param metadata The command metadata to add or update.
     */
    public static void saveCommandToConfig(String group, CommandMetadata metadata) {
        // Load existing commands using the ConfigService
        CommandMetadataWrapper commandMetadataWrapper = configService.loadCommands();

        // Remove any existing command with the same name in the specified group
        commandMetadataWrapper.removeCommandFromGroup(group, metadata.getName());

        // Add the new/updated command to the specified group
        commandMetadataWrapper.addCommandToGroup(group, metadata);

        // Save the updated commands back to the configuration file
        configService.saveCommands(commandMetadataWrapper);
    }

    /**
     * Removes a command from the configuration file using ConfigService.
     *
     * @param group       The group from which the command belongs.
     * @param commandName The name of the command to remove.
     */
    public static void removeCommandFromConfig(String group, String commandName) {
        // Load existing commands using the ConfigService
        CommandMetadataWrapper commandMetadataWrapper = configService.loadCommands();

        // Remove the specified command from the specified group
        commandMetadataWrapper.removeCommandFromGroup(group, commandName);

        // Save the updated commands back to the configuration file
        configService.saveCommands(commandMetadataWrapper);
    }

    /**
     * Retrieves all CommandMetadataWrapper(s) stored in configuration.
     *
     * @return CommandMetadataWrapper containing all command metadata grouped by categories.
     */
    public static ConfigService getConfigService() {
        return configService;
    }
}


