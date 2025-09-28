package com.helperlib.command.tunneltoggle;

import com.helperlib.api.command.ToggleCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TunnelToggleCommandRegistry {
    private static final Map<String, ToggleCommand> activeCommands = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService cleanupService =
            Executors.newScheduledThreadPool(1);

    static {
        cleanupService.scheduleAtFixedRate(
                TunnelToggleCommandRegistry::cleanupCompletedCommands,
                30, 30, TimeUnit.SECONDS);
    }

    public static ToggleCommand getOrCreateCommand(String commandId,
                                                   Supplier<ToggleCommand> supplier) {
        ToggleCommand existing = activeCommands.get(commandId);
        if (existing != null && existing.isRunning()) {
            return existing;
        }
        ToggleCommand created = supplier.get();
        activeCommands.put(commandId, created);
        return created;
    }

    private static void cleanupCompletedCommands() {
        activeCommands.entrySet().removeIf(entry -> !entry.getValue().isRunning());
    }
}