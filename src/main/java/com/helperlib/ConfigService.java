package com.helperlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import com.helperlib.command.factory.CommandRegistry;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import com.helperlib.command.*;
import com.helperlib.os.OSConfigFactory;

import static jakarta.json.stream.JsonGenerator.PRETTY_PRINTING;

public class ConfigService {

    private final String configFilePath;
    private CommandMetadataWrapper cachedCommandMetadataWrapper;

    public ConfigService() {
        var osConfigStrategy = OSConfigFactory.getOSConfigStrategy();
        this.configFilePath = osConfigStrategy.getAppFilePath();

        var folder = new File(Paths.get(this.configFilePath).getParent().toString());
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Initializes the configuration file with an empty group-to-command map if it does not exist or is empty.
     */
    public void initializeConfigFile() {
        var commandsFile = new File(configFilePath);
        if (!commandsFile.exists() || commandsFile.length() == 0) {
            // Set the cache directly to avoid reading from empty file
            cachedCommandMetadataWrapper = new CommandMetadataWrapper(new HashMap<>());
            saveCommands(cachedCommandMetadataWrapper);
        }
    }

    /**
     * Loads all group-commands from the configuration file as a `CommandMetadataWrapper` object.
     * Uses cached version if available, otherwise loads from file.
     *
     * @return A `CommandMetadataWrapper` object containing all command groups and their lists.
     */
    public CommandMetadataWrapper loadCommands() {
        if (cachedCommandMetadataWrapper == null) {
            cachedCommandMetadataWrapper = loadCommandsFromFile();
        }
        return cachedCommandMetadataWrapper;
    }

    /**
     * Reloads the CommandMetadataWrapper cache from the configuration file.
     * This method forces a refresh of the cached data.
     */
    public void reloadCache() {
        cachedCommandMetadataWrapper = loadCommandsFromFile();
    }

    /**
     * Loads commands directly from the configuration file.
     *
     * @return A `CommandMetadataWrapper` object loaded from the file.
     */
    private CommandMetadataWrapper loadCommandsFromFile() {
        File commandsFile = new File(configFilePath);

        // Return empty wrapper if file doesn't exist or is empty
        if (!commandsFile.exists() || commandsFile.length() == 0) {
            return new CommandMetadataWrapper(new HashMap<>());
        }

        try (FileInputStream fileInputStream = new FileInputStream(commandsFile);
             JsonReader jsonReader = Json.createReader(fileInputStream)) {

            JsonObject jsonObject = jsonReader.readObject();
            var groupCommands = new HashMap<String, List<CommandMetadata>>();

            jsonObject.forEach((group, commandsJson) -> {
                JsonArray commandsArray = commandsJson.asJsonArray();
                groupCommands.put(group, commandsArray.stream()
                        .map(JsonObject.class::cast)
                        .map(CommandRegistry::parseMetadata)
                        .toList());
            });

            return new CommandMetadataWrapper(groupCommands);

        } catch (IOException e) {
            throw new RuntimeException("Error reading commands from configuration file: " + e.getMessage(), e);
        } catch (Exception e) {
            // Handle case where the JSON is malformed - return empty wrapper instead of throwing
            System.err.println("Warning: Malformed JSON in configuration file: " + commandsFile.getAbsolutePath() + ". Returning empty configuration.");
            return new CommandMetadataWrapper(new HashMap<>());
        }
    }

    /**
     * Saves a `CommandMetadataWrapper` object to the configuration file and updates the cache.
     *
     * @param commandMetadataWrapper The `CommandMetadataWrapper` object containing group commands and metadata.
     */
    public void saveCommands(CommandMetadataWrapper commandMetadataWrapper) {
        // Update the cache first
        this.cachedCommandMetadataWrapper = commandMetadataWrapper;

        try (FileWriter writer = new FileWriter(configFilePath)) {

            // Use Json.createObjectBuilder to construct the JSON for groups and their commands
            var groupCommandsBuilder = Json.createObjectBuilder();

            for (Map.Entry<String, List<CommandMetadata>> entry : commandMetadataWrapper.getGroupCommands().entrySet()) {
                var groupArrayBuilder = Json.createArrayBuilder();

                for (CommandMetadata metadata : entry.getValue()) {
                    groupArrayBuilder.add(CommandRegistry.serializeMetadata(metadata));
                }

                // Add the group and its array of commands to the root object
                groupCommandsBuilder.add(entry.getKey(), groupArrayBuilder);
            }

            // Build the JSON object for the entire file
            var jsonObject = groupCommandsBuilder.build();

            // Write the JSON to the file with pretty printing enabled
            var writerFactory = Json.createWriterFactory(Map.of(PRETTY_PRINTING, true));
            var jsonWriter = writerFactory.createWriter(writer);
            jsonWriter.writeObject(jsonObject);

        } catch (IOException e) {
            throw new RuntimeException("Error saving commands to configuration file: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a command by its group and name from the cached configuration.
     *
     * @param group       The group to which the command belongs.
     * @param commandName The name of the command to retrieve.
     * @return An Optional containing the CommandMetadata if found.
     */
    public Optional<CommandMetadata> getCommandByGroupAndName(String group, String commandName) {
        CommandMetadataWrapper commandMetadataWrapper = loadCommands();
        return commandMetadataWrapper.getCommandsByGroup(group).stream()
                .filter(command -> command.getName().equals(commandName))
                .findFirst();
    }

    /**
     * Gets all commands within a specific group from the cached configuration.
     *
     * @param group The group name to retrieve all commands from.
     * @return A list of CommandMetadata within the specified group.
     */
    public List<CommandMetadata> getCommandsByGroup(String group) {
        return loadCommands().getCommandsByGroup(group);
    }

    public boolean isValidJsonFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        try (FileInputStream inputStream = new FileInputStream(file);
             JsonReader reader = Json.createReader(inputStream)) {
            reader.readObject(); // Attempt to parse the JSON
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getConfigFilePath() {
        return configFilePath;
    }
}
