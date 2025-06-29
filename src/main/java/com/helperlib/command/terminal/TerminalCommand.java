package com.helperlib.command.terminal;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.core.command.CommandExecutorService;
import com.helperlib.core.command.logging.FileStreamHandler;

import java.io.*;

import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class TerminalCommand extends Command {

    private final StreamHandler streamHandler;

    public TerminalCommand(TerminalCommandMetadata metadata, StreamHandler streamHandler) {
        super(metadata);
        this.streamHandler = streamHandler;
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            TerminalCommandMetadata terminalMetadata = (TerminalCommandMetadata) metadata;

            try {
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

                // Start stream handlers
                CompletableFuture<Void> outputHandler = streamHandler.handleStream(
                        process.getInputStream(), "stdout", metadata.getName());
                CompletableFuture<Void> errorHandler = streamHandler.handleStream(
                        process.getErrorStream(), "stderr", metadata.getName());

                // Wait for process completion
                int exitCode = process.waitFor();

                // Wait for stream readers to finish
                CompletableFuture.allOf(outputHandler, errorHandler).join();

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
}