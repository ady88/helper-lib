package com.helperlib.command.clipboard;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.core.command.CommandExecutorService;
import com.helperlib.core.command.logging.NoOpStreamHandler;

import java.util.concurrent.CompletableFuture;

public class ClipboardCommand extends Command {

    private final StreamHandler streamHandler;

    public ClipboardCommand(ClipboardCommandMetadata metadata) {
        this(metadata, new NoOpStreamHandler());
    }

    public ClipboardCommand(ClipboardCommandMetadata metadata, StreamHandler streamHandler) {
        super(metadata);
        this.streamHandler = streamHandler;
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        return CompletableFuture.supplyAsync(this::executeSync, CommandExecutorService.getVirtualThreadExecutor());
    }

    public CommandResult executeSynchronous() {
        return executeSync();
    }

    private CommandResult executeSync() {
        ClipboardCommandMetadata clipboardMetadata = (ClipboardCommandMetadata) metadata;
        String textToCopy = clipboardMetadata.getTextToCopy();

        // Use the shared clipboard service
        ClipboardResult clipboardResult = ClipboardService.copyToClipboard(textToCopy);

        // Handle empty input specifically
        if (textToCopy != null && textToCopy.isEmpty()) {
            return new CommandResult(false, -1, clipboardResult.executionTimeMs());
        }

        // Log success if needed
        if (clipboardResult.success() && streamHandler != null && !(streamHandler instanceof NoOpStreamHandler)) {
            System.out.println(clipboardResult.message());
        }

        // Convert clipboard result to command result
        if (clipboardResult.success()) {
            return new CommandResult(true, 0, clipboardResult.executionTimeMs());
        } else {
            System.err.println(clipboardResult.message());
            return new CommandResult(false, -1, clipboardResult.executionTimeMs());
        }
    }
}