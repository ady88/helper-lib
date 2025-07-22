package com.helperlib.command.terminal;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.logging.StreamHandler;
import jakarta.json.JsonObject;

/**
 * Factory implementation for TerminalCommands.
 */
public class TerminalCommandFactory implements CommandFactory {

    @Override
    public CommandMetadata parseMetadata(JsonObject jsonObject) {
        return TerminalMetadataParser.parseFromJson(jsonObject, CommandType.TERMINAL);
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata metadata) {
        return TerminalMetadataParser.serializeToJson((TerminalCommandMetadata) metadata);
    }

    @Override
    public Command createCommand(CommandMetadata metadata, StreamHandler streamHandler) {
        return new TerminalCommand((TerminalCommandMetadata) metadata, streamHandler);
    }

}


