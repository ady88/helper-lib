package com.helperlib.command.terminal;

import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for executing terminal processes with consistent configuration and stream handling.
 * Encapsulates the common logic for creating ProcessBuilder, configuring environment, and managing streams.
 */
public class TerminalProcessExecutor {
    /**
     * Executes a terminal command using the provided metadata and stream handler.
     *
     * @param metadata The terminal command metadata containing command configuration
     * @param streamHandler The stream handler for managing process output/error streams
     * @param processRef Optional atomic reference to store the created process (for cancellation)
     * @return CommandResult containing execution status, exit code, and execution time
     * @throws InterruptedException if the process execution is interrupted
     * @throws Exception if process creation or execution fails
     */
    public static CommandResult executeProcess(TerminalCommandMetadata metadata,
                                               StreamHandler streamHandler,
                                               AtomicReference<Process> processRef) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(metadata.getCommandText().split("\\s+"));

        // Add arguments to environment
        if (metadata.getArguments() != null) {
            processBuilder.environment().putAll(metadata.getArguments());
        }

        // Add PATH environment variable if specified
        if (metadata.getEnvironmentPathVariable() != null &&
                !metadata.getEnvironmentPathVariable().isEmpty()) {
            processBuilder.environment().put("PATH", metadata.getEnvironmentPathVariable());
        }

        // Set the working directory if specified
        if (metadata.getPath() != null && !metadata.getPath().isEmpty()) {
            processBuilder.directory(new File(metadata.getPath()));
        }

        Process process = processBuilder.start();

        // Store process reference if provided (for cancellation support)
        if (processRef != null) {
            processRef.set(process);
        }

        // Start stream handlers
        CompletableFuture<Void> outputHandler = streamHandler.handleStream(
                process.getInputStream(), "stdout", metadata.getName());
        CompletableFuture<Void> errorHandler = streamHandler.handleStream(
                process.getErrorStream(), "stderr", metadata.getName());

        // Wait for process completion
        int exitCode = process.waitFor();

        // Wait for stream readers to finish
        CompletableFuture.allOf(outputHandler, errorHandler).join();

        boolean success = exitCode == 0;
        return new CommandResult(success, exitCode, 0); // Execution time will be calculated by caller
    }

    /**
     * Overloaded method for simple execution without process reference storage.
     */
    public static CommandResult executeProcess(TerminalCommandMetadata metadata,
                                               StreamHandler streamHandler) throws Exception {
        return executeProcess(metadata, streamHandler, null);
    }

}
