package com.helperlib.command.terminaltoggle;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.ToggleCommand;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.command.terminal.TerminalCommandMetadata;
import com.helperlib.command.terminal.TerminalProcessExecutor;
import com.helperlib.core.command.CommandExecutorService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TerminalToggleCommand extends Command implements ToggleCommand {
    private final StreamHandler streamHandler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<CommandResult>> currentExecution = new AtomicReference<>();

    public TerminalToggleCommand(TerminalCommandMetadata metadata, StreamHandler streamHandler) {
        super(metadata);
        this.streamHandler = streamHandler;
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        // Prevent multiple simultaneous executions
        if (isRunning.get()) {
            return CompletableFuture.completedFuture(
                    new CommandResult(false, -1, 0)
            );
        }

        CompletableFuture<CommandResult> executionFuture = CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            if (!isRunning.compareAndSet(false, true)) {
                return new CommandResult(false, -1, 0);
            }

            TerminalCommandMetadata terminalMetadata = (TerminalCommandMetadata) metadata;

            try {
                System.out.println("Started toggleable terminal command: " + terminalMetadata.getName());

                CommandResult result = TerminalProcessExecutor.executeProcess(
                        terminalMetadata, streamHandler, currentProcess);

                long executionTime = System.currentTimeMillis() - startTime;

                // Log the result for debugging
                if (result.success()) {
                    System.out.println("Terminal toggle command completed successfully");
                } else if (result.exitCode() == 130 || result.exitCode() == 143) {
                    System.out.println("Terminal toggle command was cancelled");
                } else {
                    System.out.println("Terminal toggle command failed with exit code: " + result.exitCode());
                }

                return new CommandResult(result.success(), result.exitCode(), executionTime);

            } catch (InterruptedException e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("Terminal toggle command was interrupted: " + e.getMessage());
                return new CommandResult(false, 130, executionTime);
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.err.println("Terminal toggle command execution failed: " + e.getMessage());
                return new CommandResult(false, -1, executionTime);
            } finally {
                isRunning.set(false);
                currentProcess.set(null);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());

        currentExecution.set(executionFuture);
        return executionFuture;
    }

    @Override
    public CompletableFuture<CommandResult> cancelAsync() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            if (!isRunning.get()) {
                System.out.println("Command is not currently running");
                return new CommandResult(true, 0, 0);
            }

            Process process = currentProcess.get();
            if (process == null) {
                System.out.println("No active process to cancel");
                return new CommandResult(false, -1, 0);
            }

            try {
                System.out.println("Cancelling terminal toggle command: " + metadata.getName());

                // First try graceful termination
                process.destroy();

                // Wait a bit for graceful shutdown
                Thread.sleep(2000);

                // If still alive, force kill
                if (process.isAlive()) {
                    System.out.println("Force killing terminal toggle command: " + metadata.getName());
                    process.destroyForcibly();
                }

                // Wait for the execution future to complete
                CompletableFuture<CommandResult> execution = currentExecution.get();
                if (execution != null) {
                    execution.join(); // Wait for cleanup
                }

                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("Command cancelled successfully");
                return new CommandResult(true, 130, executionTime);

            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.err.println("Error cancelling terminal toggle command: " + e.getMessage());
                return new CommandResult(false, -1, executionTime);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }

    @Override
    public boolean isRunning() {
        return isRunning.get() && currentProcess.get() != null && currentProcess.get().isAlive();
    }

}
