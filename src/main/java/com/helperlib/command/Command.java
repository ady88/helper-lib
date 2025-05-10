package com.helperlib.command;

public class Command {
    private String name;
    private String command;
    private String args;

    public Command(String name, String command, String args) {
        this.name = name;
        this.command = command;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }
}