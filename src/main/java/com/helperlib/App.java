package com.helperlib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.helperlib.command.Command;
import java.io.IOException;

public class App {

    private final ConfigService configService;

    public App() {
        this.configService = new ConfigService();
        this.configService.initializeConfigFile();
    }

    public String executeCommand(String... command) {
        var output = new StringBuilder();
        try {
            var processBuilder = new ProcessBuilder(command);
            var process = processBuilder.start();

            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();

            var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            var errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            errorReader.close();

            var exitCode = process.waitFor();
            if (exitCode != 0) {
                return "Error: " + errorOutput.toString();
            }
        } catch (IOException | InterruptedException e) {
            return "Exception: " + e.getMessage();
        }
        return output.toString();
    }

    public String executeCommandFromConfig(String commandName) {
        Command commandObj = configService.getCommandByName(commandName);
        if (commandObj != null) {
            return executeCommand(commandObj.getCommand(), commandObj.getArgs());
        }
        return "Command not found: " + commandName;
    }
}
