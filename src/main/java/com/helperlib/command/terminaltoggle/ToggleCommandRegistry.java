package com.helperlib.command.terminaltoggle;

import com.helperlib.api.command.ToggleCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ToggleCommandRegistry {
    private static final Map<String, ToggleCommand> activeCommands = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService cleanupService =
            Executors.newScheduledThreadPool(1);

    static {
        // Periodic cleanup of completed commands
        cleanupService.scheduleAtFixedRate(
                ToggleCommandRegistry::cleanupCompletedCommands,
                30, 30, TimeUnit.SECONDS);
    }

    public static boolean isCommandRunning(String commandId) {
        ToggleCommand command = activeCommands.get(commandId);
        return command != null && command.isRunning();
    }

    public static ToggleCommand getOrCreateCommand(String commandId,
                                                   Supplier<ToggleCommand> commandSupplier) {

        ToggleCommand existing = activeCommands.get(commandId);
        if (existing != null && existing.isRunning()) {
            return existing;
        }

        ToggleCommand newCommand = commandSupplier.get();
        activeCommands.put(commandId, newCommand);
        return newCommand;
    }

    public static void registerCommand(String commandId, ToggleCommand command) {
        activeCommands.put(commandId, command);
    }

    public static void unregisterCommand(String commandId) {
        activeCommands.remove(commandId);
    }

    private static void cleanupCompletedCommands() {
        activeCommands.entrySet().removeIf(entry -> !entry.getValue().isRunning());
    }
}

