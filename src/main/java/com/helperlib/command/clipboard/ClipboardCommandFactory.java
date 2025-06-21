package com.helperlib.command.clipboard;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import jakarta.json.Json;
import jakarta.json.JsonObject;


public class ClipboardCommandFactory implements CommandFactory {

    @Override
    public CommandMetadata parseMetadata(JsonObject jsonObject) {
        return new ClipboardCommandMetadata(
                jsonObject.getString("name"),
                jsonObject.getString("description"),
                jsonObject.getString("textToCopy")
        );
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata metadata) {
        ClipboardCommandMetadata clipboard = (ClipboardCommandMetadata) metadata;
        return Json.createObjectBuilder()
                .add("name", clipboard.getName())
                .add("description", clipboard.getDescription())
                .add("type", CommandType.CLIPBOARD.toString())
                .add("textToCopy", clipboard.getTextToCopy())
                .build();
    }

    @Override
    public Command createCommand(CommandMetadata metadata) {
        return new ClipboardCommand((ClipboardCommandMetadata) metadata);
    }
}

