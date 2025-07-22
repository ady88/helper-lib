package com.helperlib.command.terminal;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;

import java.util.Map;

public class TerminalCommandMetadata extends CommandMetadata {
    private String commandText;
    private Map<String, String> arguments;
    private String path;
    private String environmentPathVariable;

    public TerminalCommandMetadata(String name, String description, String commandText, Map<String, String> arguments, String path, String environmentPathVariable) {
        super(name, description, CommandType.TERMINAL);
        this.commandText = commandText;
        this.arguments = arguments;
        this.path = path;
        this.environmentPathVariable = environmentPathVariable; // Initialize new variable
    }

    // New constructor that accepts CommandType
    public TerminalCommandMetadata(String name, String description, CommandType commandType, String commandText, Map<String, String> arguments, String path, String environmentPathVariable) {
        super(name, description, commandType);
        this.commandText = commandText;
        this.arguments = arguments;
        this.path = path;
        this.environmentPathVariable = environmentPathVariable;
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

    public String getEnvironmentPathVariable() {
        return environmentPathVariable; // Getter for the new variable
    }

    public void setEnvironmentPathVariable(String environmentPathVariable) {
        this.environmentPathVariable = environmentPathVariable; // Setter for the new variable
    }
}

