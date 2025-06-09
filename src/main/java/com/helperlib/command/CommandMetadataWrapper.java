package com.helperlib.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandMetadataWrapper {
    private Map<String, List<CommandMetadata>> groupCommands;

    public CommandMetadataWrapper() {
        this.groupCommands = new HashMap<>();
    }

    public CommandMetadataWrapper(Map<String, List<CommandMetadata>> groupCommands) {
        this.groupCommands = groupCommands;
    }

    public Map<String, List<CommandMetadata>> getGroupCommands() {
        return groupCommands;
    }

    public void setGroupCommands(Map<String, List<CommandMetadata>> groupCommands) {
        this.groupCommands = groupCommands;
    }

    public List<CommandMetadata> getCommandsByGroup(String group) {
        return groupCommands.getOrDefault(group, List.of());
    }

    public void addCommandToGroup(String group, CommandMetadata command) {
        groupCommands.computeIfAbsent(group, k -> new ArrayList<>()).add(command);
    }

    public void removeCommandFromGroup(String group, String commandName) {
        groupCommands.computeIfPresent(group, (k, commands) -> {
            List<CommandMetadata> mutableList = new ArrayList<>(commands); // Convert to a mutable list
            mutableList.removeIf(command -> command.getName().equals(commandName));
            return mutableList.isEmpty() ? null : mutableList; // Remove group if no commands left
        });
    }

}
