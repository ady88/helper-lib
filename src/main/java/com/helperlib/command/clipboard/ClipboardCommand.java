package com.helperlib.command.clipboard;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.core.command.CommandExecutorService;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.CompletableFuture;

public class ClipboardCommand extends Command {

    public ClipboardCommand(ClipboardCommandMetadata metadata) {
        super(metadata);
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            ClipboardCommandMetadata clipboardMetadata = (ClipboardCommandMetadata) metadata;

            try {
                StringSelection stringSelection = new StringSelection(clipboardMetadata.getTextToCopy());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
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

