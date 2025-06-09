package com.helperlib.command.factory;

import com.helperlib.command.Command;
import com.helperlib.command.CommandMetadata;
import jakarta.json.JsonObject;

public interface CommandFactory {
    CommandMetadata parseMetadata(JsonObject jsonObject);
    JsonObject serializeMetadata(CommandMetadata metadata);
    Command createCommand(CommandMetadata metadata);
}

