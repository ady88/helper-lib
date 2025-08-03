package com.helperlib.command.terminaltoggle;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.command.terminal.TerminalCommandMetadata;
import com.helperlib.command.terminal.TerminalMetadataParser;

import jakarta.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory implementation for TerminalToggleCommands.
 */
public class TerminalToggleCommandFactory implements CommandFactory {
    // Thread-safe map to track running commands
    private static final Map<String, TerminalToggleCommand> runningCommands = new ConcurrentHashMap<>();

    @Override
    public CommandMetadata parseMetadata(JsonObject jsonObject) {
        return TerminalMetadataParser.parseFromJson(jsonObject, CommandType.TERMINAL_TOGGLE);
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata metadata) {
        return TerminalMetadataParser.serializeToJson((TerminalCommandMetadata) metadata);
    }

    // Factory handles the registry logic
    @Override
    public Command createCommand(CommandMetadata metadata, StreamHandler streamHandler) {
        String commandId = generateCommandId(metadata);

        // Check if already running
        TerminalToggleCommand existingCommand = runningCommands.get(commandId);
        if (existingCommand != null && existingCommand.isRunning()) {
            return existingCommand; // Return existing instance
        }

        // Create new command - unchanged constructor
        TerminalToggleCommand newCommand = new TerminalToggleCommand(
                (TerminalCommandMetadata) metadata, streamHandler);
        runningCommands.put(commandId, newCommand);

        return newCommand;
    }



    private String generateCommandId(CommandMetadata metadata) {
        TerminalCommandMetadata terminalMetadata = (TerminalCommandMetadata) metadata;

        // Use name + command text + path for uniqueness
        return String.format("%s-%s-%s",
                terminalMetadata.getName(),
                terminalMetadata.getCommandText().hashCode(),
                terminalMetadata.getPath().hashCode());
    }


    private void cleanupCompletedCommands() {
        runningCommands.entrySet().removeIf(entry -> !entry.getValue().isRunning());
    }

    // Optional: Get running command by ID
    public static TerminalToggleCommand getRunningCommand(String commandId) {
        return runningCommands.get(commandId);
    }

}
