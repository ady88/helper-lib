package com.helperlib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.helperlib.os.OSConfigFactory;
import com.helperlib.command.Command;
import com.helperlib.command.Commands;

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

    public void initializeConfigFile() {
        var commandsFile = new File(configFilePath);
        if (!commandsFile.exists() || commandsFile.length() == 0) {
            try (var writer = new FileWriter(commandsFile)) {
                var commands = new Commands(new ArrayList<>());
                var jsonObject = new JSONObject();
                jsonObject.put("commands", new JSONArray(commands.getCommands()));
                writer.write(jsonObject.toString(4)); // Pretty print JSON with indentation
            } catch (IOException e) {
                throw new RuntimeException("Error creating commands.json: " + e.getMessage(), e);
            }
        }
    }

    public Command getCommandByName(String commandName) {
        var commands = getAllCommands();
        return commands.getCommands().stream()
            .filter(command -> command.getName().equals(commandName))
            .findFirst()
            .orElse(null);
    }

    public Commands getAllCommands() {
        try {
            var jsonContent = FileUtils.readFileToString(new File(configFilePath), "UTF-8");
            var commandsJson = new JSONObject(jsonContent);
            var commandsArray = commandsJson.getJSONArray("commands");

            var commandList = new ArrayList<Command>();
            for (int i = 0; i < commandsArray.length(); i++) {
                var commandObj = commandsArray.getJSONObject(i);
                commandList.add(new Command(
                    commandObj.getString("name"),
                    commandObj.getString("command"),
                    commandObj.getString("args")
                ));
            }
            return new Commands(commandList);
        } catch (IOException e) {
            throw new RuntimeException("Error reading commands.json: " + e.getMessage(), e);
        }
    }
}