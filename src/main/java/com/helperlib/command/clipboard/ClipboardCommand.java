package com.helperlib.command.clipboard;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.core.command.CommandExecutorService;
import com.helperlib.core.command.logging.NoOpStreamHandler;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.CompletableFuture;

public class ClipboardCommand extends Command {

    private final StreamHandler streamHandler;

    public ClipboardCommand(ClipboardCommandMetadata metadata) {
        this(metadata, new NoOpStreamHandler());
    }

    public ClipboardCommand(ClipboardCommandMetadata metadata, StreamHandler streamHandler) {
        super(metadata);
        this.streamHandler = streamHandler; // Fix: Store the handler
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            ClipboardCommandMetadata clipboardMetadata = (ClipboardCommandMetadata) metadata;

            try {
                StringSelection stringSelection = new StringSelection(clipboardMetadata.getTextToCopy());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

                // Keep simple console output since StreamHandler is for stream processing
                System.out.println("Text copied to clipboard successfully.");

                long executionTime = System.currentTimeMillis() - startTime;
                return new CommandResult(true, 0, executionTime);

            } catch (Exception e) {
                System.err.println("Error copying text to clipboard: " + e.getMessage());
                long executionTime = System.currentTimeMillis() - startTime;
                return new CommandResult(false, -1, executionTime);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }
}
