package com.helperlib.command.terminal;

import com.helperlib.api.command.CommandType;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for parsing and serializing terminal command metadata from/to JSON.
 * Handles common JSON operations for both TerminalCommand and TerminalToggleCommand factories.
 */
public class TerminalMetadataParser {

    /**
     * Parses a JsonObject into TerminalCommandMetadata with the specified command type.
     *
     * @param jsonObject The JSON object containing metadata fields
     * @param commandType The type of command (TERMINAL or TERMINAL_TOGGLE)
     * @return TerminalCommandMetadata with parsed values
     */
    public static TerminalCommandMetadata parseFromJson(JsonObject jsonObject, CommandType commandType) {
        String name = jsonObject.getString("name");
        String description = jsonObject.getString("description");
        String commandText = jsonObject.getString("commandText", "");
        JsonObject argumentsJson = jsonObject.getJsonObject("arguments");
        Map<String, String> arguments = parseArguments(argumentsJson);
        String path = jsonObject.getString("path", "");
        String environmentPathVariable = jsonObject.getString("environmentPathVariable", "");

        return new TerminalCommandMetadata(name, description, commandType, commandText, arguments, path, environmentPathVariable);
    }

    /**
     * Serializes TerminalCommandMetadata into a JsonObject.
     *
     * @param metadata The terminal command metadata to serialize
     * @return JsonObject containing all metadata fields
     */
    public static JsonObject serializeToJson(TerminalCommandMetadata metadata) {
        return Json.createObjectBuilder()
                .add("name", metadata.getName())
                .add("description", metadata.getDescription())
                .add("type", metadata.getType().toString())
                .add("commandText", metadata.getCommandText())
                .add("arguments", serializeArguments(metadata.getArguments()))
                .add("path", metadata.getPath())
                .add("environmentPathVariable", metadata.getEnvironmentPathVariable())
                .build();
    }

    /**
     * Parses a JsonObject into a Map of arguments.
     *
     * @param argumentsObject The JSON object containing arguments, may be null
     * @return Map of argument key-value pairs, empty map if argumentsObject is null
     */
    private static Map<String, String> parseArguments(JsonObject argumentsObject) {
        if (argumentsObject == null) {
            return Map.of();
        }
        return argumentsObject.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }

    /**
     * Serializes a Map of arguments into a JsonObject.
     *
     * @param arguments The argument map to serialize
     * @return JsonObject containing all arguments
     */
    private static JsonObject serializeArguments(Map<String, String> arguments) {
        var argumentsBuilder = Json.createObjectBuilder();
        arguments.forEach(argumentsBuilder::add);
        return argumentsBuilder.build();
    }
}

