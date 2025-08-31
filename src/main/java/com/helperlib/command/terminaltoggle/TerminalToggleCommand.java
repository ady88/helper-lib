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

    // State for the main/start command
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<CommandResult>> currentExecution = new AtomicReference<>();

    // State for the toggle command
    private final AtomicBoolean isToggling = new AtomicBoolean(false);
    private final AtomicReference<Process> currentToggleProcess = new AtomicReference<>();
    private final AtomicReference<CompletableFuture<CommandResult>> currentToggleExecution = new AtomicReference<>();

    public TerminalToggleCommand(TerminalToggleCommandMetadata metadata, StreamHandler streamHandler) {
        super(metadata);
        this.streamHandler = streamHandler;
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        // Prevent multiple simultaneous main/start executions
        if (isRunning.get()) {
            return CompletableFuture.completedFuture(new CommandResult(false, -1, 0));
        }

        CompletableFuture<CommandResult> executionFuture = CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            if (!isRunning.compareAndSet(false, true)) {
                return new CommandResult(false, -1, 0);
            }

            TerminalToggleCommandMetadata terminalMetadata = (TerminalToggleCommandMetadata) metadata;

            try {
                System.out.println("Started toggleable terminal command: " + terminalMetadata.getName());

                CommandResult result = TerminalProcessExecutor.executeProcess(
                        terminalMetadata, streamHandler, currentProcess);

                long executionTime = System.currentTimeMillis() - startTime;

                if (result.success()) {
                    System.out.println("Terminal toggle command (main/start) completed successfully");
                } else {
                    System.out.println("Terminal toggle command (main/start) failed with exit code: " + result.exitCode());
                }

                return new CommandResult(result.success(), result.exitCode(), executionTime);
            } catch (InterruptedException e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("Terminal toggle command (main/start) was interrupted: " + e.getMessage());
                return new CommandResult(false, 130, executionTime);
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.err.println("Terminal toggle command (main/start) execution failed: " + e.getMessage());
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
    public CompletableFuture<CommandResult> toggleAsync() {
        // Prevent multiple simultaneous toggles, but allow toggling while the main command may be running
        if (isToggling.get()) {
            return CompletableFuture.completedFuture(new CommandResult(false, -1, 0));
        }

        CompletableFuture<CommandResult> toggleFuture = CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            if (!isToggling.compareAndSet(false, true)) {
                return new CommandResult(false, -1, 0);
            }

            TerminalToggleCommandMetadata meta = (TerminalToggleCommandMetadata) metadata;
            String toggleCmd = meta.getToggleCommandText();

            if (toggleCmd == null || toggleCmd.isBlank()) {
                isToggling.set(false);
                return new CommandResult(false, -1, 0);
            }

            // Build a temporary metadata for the toggle command execution
            TerminalCommandMetadata toggleMetadata = new TerminalCommandMetadata(
                    meta.getName() + " (toggle)",
                    meta.getDescription(),
                    meta.getType(),
                    toggleCmd,
                    meta.getArguments(),
                    meta.getPath(),
                    meta.getEnvironmentPathVariable()
            );

            try {
                System.out.println("Running toggle command for: " + meta.getName());

                CommandResult result = TerminalProcessExecutor.executeProcess(
                        toggleMetadata, streamHandler, currentToggleProcess);

                long executionTime = System.currentTimeMillis() - startTime;

                if (result.success()) {
                    System.out.println("Toggle command completed successfully");
                } else {
                    System.out.println("Toggle command failed with exit code: " + result.exitCode());
                }

                return new CommandResult(result.success(), result.exitCode(), executionTime);
            } catch (InterruptedException e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("Toggle command was interrupted: " + e.getMessage());
                return new CommandResult(false, 130, executionTime);
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                System.err.println("Toggle command execution failed: " + e.getMessage());
                return new CommandResult(false, -1, executionTime);
            } finally {
                isToggling.set(false);
                currentToggleProcess.set(null);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());

        currentToggleExecution.set(toggleFuture);
        return toggleFuture;
    }

    @Override
    public boolean isRunning() {
        Process p = currentProcess.get();
        return isRunning.get() && p != null && p.isAlive();
    }
}