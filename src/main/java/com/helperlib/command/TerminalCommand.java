package com.helperlib.command;

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
        var output = new StringBuilder();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commandText);

            if (arguments != null) {
                processBuilder.environment().putAll(arguments);
            }

            if (path != null && !path.isEmpty()) {
                processBuilder.directory(new java.io.File(path));
            }

            var process = processBuilder.start();
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


