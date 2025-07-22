package com.helperlib.command.terminal;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.core.command.CommandExecutorService;

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
                CommandResult result = TerminalProcessExecutor.executeProcess(terminalMetadata, streamHandler);
                long executionTime = System.currentTimeMillis() - startTime;

                // Return new result with actual execution time
                return new CommandResult(result.success(), result.exitCode(), executionTime);

            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.err.println("Command execution failed: " + e.getMessage());
                return new CommandResult(false, -1, executionTime);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }
}
