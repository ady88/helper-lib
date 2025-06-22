package com.helperlib.command.terminal;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.core.command.CommandExecutorService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

public class TerminalCommand extends Command {

    public TerminalCommand(TerminalCommandMetadata metadata) {
        super(metadata);
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            TerminalCommandMetadata terminalMetadata = (TerminalCommandMetadata) metadata;

            try {
                // Create output file paths based on commandName_stdout/stderr pattern
                Path outputFile = createOutputFilePath("stdout");
                Path errorFile = createOutputFilePath("stderr");

                // Ensure directories exist
                Files.createDirectories(outputFile.getParent());

                ProcessBuilder processBuilder = new ProcessBuilder(terminalMetadata.getCommandText().split("\\s+"));

                // Add arguments to environment
                if (terminalMetadata.getArguments() != null) {
                    processBuilder.environment().putAll(terminalMetadata.getArguments());
                }

                // Add PATH environment variable if specified
                if (terminalMetadata.getEnvironmentPathVariable() != null &&
                        !terminalMetadata.getEnvironmentPathVariable().isEmpty()) {
                    processBuilder.environment().put("PATH", terminalMetadata.getEnvironmentPathVariable());
                }

                // Set the working directory if specified
                if (terminalMetadata.getPath() != null && !terminalMetadata.getPath().isEmpty()) {
                    processBuilder.directory(new File(terminalMetadata.getPath()));
                }

                Process process = processBuilder.start();

                // Start real-time stream readers
                CompletableFuture<Void> outputReader = streamToFile(
                        process.getInputStream(), outputFile);
                CompletableFuture<Void> errorReader = streamToFile(
                        process.getErrorStream(), errorFile);

                // Wait for process completion
                int exitCode = process.waitFor();

                // Wait for stream readers to finish
                CompletableFuture.allOf(outputReader, errorReader).join();

                long executionTime = System.currentTimeMillis() - startTime;
                boolean success = exitCode == 0;

                return new CommandResult(success, exitCode, executionTime);

            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.err.println("Command execution failed: " + e.getMessage());
                return new CommandResult(false, -1, executionTime);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }

    private Path createOutputFilePath(String streamType) {
        String fileName = String.format("%s_%s.log",
                sanitizeFileName(metadata.getName()),
                streamType);

        return Paths.get("logs", fileName);
    }

    private String sanitizeFileName(String input) {
        // Remove or replace invalid filename characters
        return input.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private CompletableFuture<Void> streamToFile(InputStream inputStream, Path outputPath) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 BufferedWriter writer = Files.newBufferedWriter(outputPath,
                         StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    writer.flush(); // Ensure real-time writing
                }
            } catch (IOException e) {
                System.err.println("Error streaming to file " + outputPath + ": " + e.getMessage());
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }
}