package com.helperlib.command.terminal;

import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.command.clipboard.ClipboardService;

import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for executing terminal processes with consistent configuration and stream handling.
 * Encapsulates the common logic for creating ProcessBuilder, configuring environment, and managing streams.
 */
public class TerminalProcessExecutor {

    public static CommandResult executeProcess(TerminalCommandMetadata metadata,
                                               StreamHandler streamHandler,
                                               AtomicReference<Process> processRef) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(buildPlatformCommand(metadata.getCommandText()));

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

        // Capture single-line output for clipboard
        final AtomicReference<String> singleLineOutput = new AtomicReference<>();

        CompletableFuture<Void> outputHandler = CompletableFuture.runAsync(() ->
                processOutputStream(process.getInputStream(), streamHandler, metadata, singleLineOutput));

        // Handle stderr stream
        CompletableFuture<Void> errorHandler = streamHandler.handleStream(
                process.getErrorStream(), "stderr", metadata.getName());

        // Wait for process completion
        int exitCode = process.waitFor();

        // Wait for stream readers to finish
        CompletableFuture.allOf(outputHandler, errorHandler).join();

        // Copy to clipboard if output was exactly one line
        if (exitCode == 0 && singleLineOutput.get() != null) {
            String lineToClipboard = singleLineOutput.get().trim();
            if (!lineToClipboard.isEmpty()) {
                ClipboardService.copyToClipboardSilent(lineToClipboard);
            }
        }

        boolean success = exitCode == 0;
        return new CommandResult(success, exitCode, 0);
    }


    /**
     * Processes the output stream, capturing output for clipboard if it's exactly one line
     * and forwarding all output to the stream handler.
     */
    private static void processOutputStream(InputStream inputStream,
                                            StreamHandler streamHandler,
                                            TerminalCommandMetadata metadata,
                                            AtomicReference<String> firstLine) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = reader.readLine(); // Read first line

            // Check if there's a second line to determine if output is single-line
            String secondLine = reader.readLine();

            // If there's only one line, capture it for clipboard
            if (line != null && secondLine == null) {
                firstLine.set(line);
            }

            // Reconstruct all output for the stream handler
            StringBuilder allOutput = new StringBuilder();
            if (line != null) {
                allOutput.append(line).append(System.lineSeparator());
            }
            if (secondLine != null) {
                allOutput.append(secondLine).append(System.lineSeparator());

                // Read any remaining lines
                String nextLine;
                while ((nextLine = reader.readLine()) != null) {
                    allOutput.append(nextLine).append(System.lineSeparator());
                }
            }

            // Send to stream handler
            if (allOutput.length() > 0) {
                try (ByteArrayInputStream recreatedStream =
                             new ByteArrayInputStream(allOutput.toString().getBytes())) {
                    streamHandler.handleStream(recreatedStream, "stdout", metadata.getName()).join();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error handling stdout: " + e.getMessage(), e);
        }
    }

    public static CommandResult executeProcess(TerminalCommandMetadata metadata,
                                               StreamHandler streamHandler) throws Exception {
        return executeProcess(metadata, streamHandler, null);
    }

    public static List<String> buildPlatformCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            throw new IllegalArgumentException("rawCommand must not be null or blank");
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return List.of("cmd.exe", "/c", rawCommand);
        } else {
            return List.of("/bin/sh", "-c", rawCommand);
        }
    }
}