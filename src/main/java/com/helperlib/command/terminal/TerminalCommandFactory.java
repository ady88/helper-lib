package com.helperlib.command.terminal;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory implementation for TerminalCommands.
 */
public class TerminalCommandFactory implements CommandFactory {

    @Override
    public CommandMetadata parseMetadata(JsonObject jsonObject) {
        // Parse TerminalCommandMetadata from the JsonObject
        String name = jsonObject.getString("name");
        String description = jsonObject.getString("description");
        String commandText = jsonObject.getString("commandText", ""); // Default to empty
        JsonObject argumentsJson = jsonObject.getJsonObject("arguments");
        Map<String, String> arguments = parseArguments(argumentsJson);
        String path = jsonObject.getString("path", ""); // Default to empty path
        String environmentPathVariable = jsonObject.getString("environmentPathVariable", ""); // New

        return new TerminalCommandMetadata(name, description, commandText, arguments, path, environmentPathVariable);
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata metadata) {
        // Serialize TerminalCommandMetadata into a JsonObject
        TerminalCommandMetadata terminalMetadata = (TerminalCommandMetadata) metadata;

        return Json.createObjectBuilder()
                .add("name", terminalMetadata.getName())
                .add("description", terminalMetadata.getDescription())
                .add("type", CommandType.TERMINAL.toString())
                .add("commandText", terminalMetadata.getCommandText())
                .add("arguments", serializeArguments(terminalMetadata.getArguments()))
                .add("path", terminalMetadata.getPath())
                .add("environmentPathVariable", terminalMetadata.getEnvironmentPathVariable()) // Serialize new variable
                .build();
    }

    @Override
    public Command createCommand(CommandMetadata metadata) {
        // Create a TerminalCommand object using the metadata
        return new TerminalCommand((TerminalCommandMetadata) metadata);
    }

    /**
     * Parses a JsonObject into a Map of arguments.
     */
    private Map<String, String> parseArguments(JsonObject argumentsObject) {
        if (argumentsObject == null) {
            return Map.of(); // Return an empty map if no arguments are defined
        }
        return argumentsObject.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }

    /**
     * Serializes a Map of arguments into a JsonObject.
     */
    private JsonObject serializeArguments(Map<String, String> arguments) {
        var argumentsBuilder = Json.createObjectBuilder();
        arguments.forEach(argumentsBuilder::add);
        return argumentsBuilder.build();
    }
}


