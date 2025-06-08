package com.helperlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.helperlib.command.*;
import com.helperlib.os.OSConfigFactory;

public class ConfigService {

    private final String configFilePath;

    public ConfigService() {
        var osConfigStrategy = OSConfigFactory.getOSConfigStrategy();
        this.configFilePath = osConfigStrategy.getAppFilePath();

        var folder = new File(Paths.get(this.configFilePath).getParent().toString());
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Initializes the configuration file with an empty commands list if it does not exist or is empty.
     */
    public void initializeConfigFile() {
        var commandsFile = new File(configFilePath);
        if (!commandsFile.exists() || commandsFile.length() == 0) {
            try (var writer = new FileWriter(commandsFile)) {
                var jsonObjectBuilder = Json.createObjectBuilder()
                        .add("commands", Json.createArrayBuilder());

                // Write the JSON to the file with pretty printing
                var writerFactory = Json.createWriterFactory(
                        Map.of(javax.json.stream.JsonGenerator.PRETTY_PRINTING, true));
                var jsonWriter = writerFactory.createWriter(writer);
                jsonWriter.writeObject(jsonObjectBuilder.build());
                jsonWriter.close();
            } catch (IOException e) {
                throw new RuntimeException("Error creating commands.json: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Retrieves a CommandMetadata object by its name.
     *
     * @param commandName The name of the command to retrieve.
     * @return An Optional containing the CommandMetadata if the command exists.
     */
    public Optional<CommandMetadata> getCommandMetadataByName(String commandName) {
        var commands = getAllCommandMetadata();
        return commands.stream()
                .filter(metadata -> metadata.getName().equals(commandName))
                .findFirst();
    }

    /**$
     * Retrieves all the CommandMetadata objects from the configuration file.
     *
     * @return A list of CommandMetadata objects.
     */
    public List<CommandMetadata> getAllCommandMetadata() {
        try (JsonReader reader = Json.createReader(new FileInputStream(configFilePath))) {
            // Read the JSON file directly into a JsonObject$$
            JsonObject jsonObject = reader.readObject();

            // Get the commands array
            JsonArray commandsArray = jsonObject.getJsonArray("commands");

            // Map each JsonObject in the array to a CommandMetadata object
            return commandsArray.stream()
                    .map(JsonObject.class::cast)
                    .map(this::parseCommandMetadata)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading commands.json: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a JsonObject into a specific CommandMetadata object based on the command type.
     *
     * @param jsonObject The JsonObject representing the command.
     * @return The corresponding CommandMetadata object.
     */
    private CommandMetadata parseCommandMetadata(JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        String description = jsonObject.getString("description");
        String type = jsonObject.getString("type");

        return switch (type) {
            case "CLIPBOARD" -> new ClipboardCommandMetadata(
                    name,
                    description,
                    jsonObject.getString("textToCopy")
            );
            case "TERMINAL" -> new TerminalCommandMetadata(
                    name,
                    description,
                    jsonObject.getString("commandText"),
                    parseArguments(jsonObject.getJsonObject("arguments")),
                    jsonObject.getString("path", "") // Optional, default to ""
            );
            default -> throw new IllegalArgumentException("Unsupported command type: " + type);
        };
    }

    /**
     * Parses a JsonObject into a Map of arguments for a terminal command.
     *
     * @param argumentsObject The JsonObject representing the arguments.
     * @return A Map of argument key-value pairs.
     */
    private Map<String, String> parseArguments(JsonObject argumentsObject) {
        if (argumentsObject == null) {
            return Map.of(); // Return an empty map if no arguments are defined
        }
        return argumentsObject.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }

    /**
     * Saves CommandMetadata objects to the configuration file.
     *
     * @param commands The list of CommandMetadata objects to write.
     */
    public void saveAllCommandMetadata(List<CommandMetadata> commands) {
        try (var writer = new FileWriter(configFilePath)) {
            var commandArrayBuilder = Json.createArrayBuilder();

            for (var metadata : commands) {
                commandArrayBuilder.add(serializeCommandMetadata(metadata));
            }

            var jsonObject = Json.createObjectBuilder()
                    .add("commands", commandArrayBuilder)
                    .build();

            // Write the JSON to the file with pretty printing
            var writerFactory = Json.createWriterFactory(
                    Map.of(javax.json.stream.JsonGenerator.PRETTY_PRINTING, true));
            var jsonWriter = writerFactory.createWriter(writer);
            jsonWriter.writeObject(jsonObject);
        } catch (IOException e) {
            throw new RuntimeException("Error saving commands.json: " + e.getMessage(), e);
        }
    }

    /**
     * Serializes a CommandMetadata object into a JsonObject.
     *
     * @param metadata The CommandMetadata object to serialize.
     * @return The resulting JsonObject.
     */
    private JsonObject serializeCommandMetadata(CommandMetadata metadata) {
        var jsonObjectBuilder = Json.createObjectBuilder()
                .add("name", metadata.getName())
                .add("description", metadata.getDescription())
                .add("type", metadata.getType().toString());

        return switch (metadata) {
            case ClipboardCommandMetadata clipboardMetadata -> jsonObjectBuilder
                    .add("textToCopy", clipboardMetadata.getTextToCopy())
                    .build();

            case TerminalCommandMetadata terminalMetadata -> jsonObjectBuilder
                    .add("commandText", terminalMetadata.getCommandText())
                    .add("arguments", serializeArguments(terminalMetadata.getArguments()))
                    .add("path", terminalMetadata.getPath())
                    .build();

            default -> throw new IllegalArgumentException("Unsupported CommandMetadata type: " + metadata.getClass().getSimpleName());
        };
    }

    /**
     * Serializes a Map of arguments into a JsonObject.
     *
     * @param arguments The Map of arguments to serialize.
     * @return The resulting JsonObject.
     */
    private JsonObject serializeArguments(Map<String, String> arguments) {
        var argumentsBuilder = Json.createObjectBuilder();
        arguments.forEach(argumentsBuilder::add);
        return argumentsBuilder.build();
    }

}
