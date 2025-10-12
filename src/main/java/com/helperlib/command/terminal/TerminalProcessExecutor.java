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
                                            AtomicReference<String> firstLineRef) {
        final String newline = System.lineSeparator();
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                PipedOutputStream pos = new PipedOutputStream();
                PipedInputStream pis = new PipedInputStream(pos, 8192)
        ) {
            // Start handler immediately so consumer can read while we write
            CompletableFuture<Void> handlerFuture = streamHandler.handleStream(pis, "stdout", metadata.getName());

            String line1 = reader.readLine();
            String line2 = null;
            if (line1 != null) {
                line2 = reader.readLine();
            }

            if (line1 != null && line2 == null) {
                // Single line total
                firstLineRef.set(line1);
            }

            // Write what we have, then continue streaming
            if (line1 != null) {
                pos.write(line1.getBytes());
                pos.write(newline.getBytes());
            }
            if (line2 != null) {
                pos.write(line2.getBytes());
                pos.write(newline.getBytes());

                String next;
                while ((next = reader.readLine()) != null) {
                    pos.write(next.getBytes());
                    pos.write(newline.getBytes());
                }
            }
            pos.flush();

            // Close writer to signal EOF to handler and wait for it
            pos.close();
            handlerFuture.join();
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