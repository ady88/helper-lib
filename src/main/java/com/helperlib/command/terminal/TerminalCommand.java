package com.helperlib.command.terminal;

import com.helperlib.api.command.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;

public class TerminalCommand extends Command {

    public TerminalCommand(TerminalCommandMetadata metadata) {
        super(metadata);
    }

    @Override
    public void execute() {
        TerminalCommandMetadata terminalMetadata = (TerminalCommandMetadata) metadata;
        String commandText = terminalMetadata.getCommandText();
        Map<String, String> arguments = terminalMetadata.getArguments();
        String path = terminalMetadata.getPath();
        String environmentPathVariable = terminalMetadata.getEnvironmentPathVariable();

        var output = new StringBuilder();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commandText);

            // Add arguments to environment
            if (arguments != null) {
                processBuilder.environment().putAll(arguments);
            }

            // Add PATH environment variable if specified
            if (environmentPathVariable != null && !environmentPathVariable.isEmpty()) {
                processBuilder.environment().put("PATH", environmentPathVariable);
            }

            // Set the working directory if specified
            if (path != null && !path.isEmpty()) {
                processBuilder.directory(new java.io.File(path));
            }

            // Start process
            var process = processBuilder.start();

            // Read output
            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Error occurred while executing command.");
            } else {
                System.out.println("Execution successful: " + output);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}



