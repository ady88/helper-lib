package com.helperlib.command;

public abstract class Command {
    protected CommandMetadata metadata;

    public Command(CommandMetadata metadata) {
        this.metadata = metadata;
    }

    public CommandMetadata getMetadata() {
        return metadata;
    }

    public abstract void execute();
}

