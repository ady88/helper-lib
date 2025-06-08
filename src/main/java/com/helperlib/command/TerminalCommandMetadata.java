package com.helperlib.command;

import java.util.Map;

public class TerminalCommandMetadata extends CommandMetadata {
    private String commandText;
    private Map<String, String> arguments;
    private String path;

    public TerminalCommandMetadata(String name, String description, String commandText, Map<String, String> arguments, String path) {
        super(name, description, CommandType.TERMINAL);
        this.commandText = commandText;
        this.arguments = arguments;
        this.path = path;
    }

    public String getCommandText() {
        return commandText;
    }

    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

