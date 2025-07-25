package com.helperlib.command.terminaltoggle;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.command.terminal.TerminalCommandMetadata;
import com.helperlib.command.terminal.TerminalMetadataParser;

import jakarta.json.JsonObject;

/**
 * Factory implementation for TerminalToggleCommands.
 */
public class TerminalToggleCommandFactory implements CommandFactory {
    @Override
    public CommandMetadata parseMetadata(JsonObject jsonObject) {
        return TerminalMetadataParser.parseFromJson(jsonObject, CommandType.TERMINAL_TOGGLE);
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata metadata) {
        return TerminalMetadataParser.serializeToJson((TerminalCommandMetadata) metadata);
    }

    @Override
    public Command createCommand(CommandMetadata metadata, StreamHandler streamHandler) {
        return new TerminalToggleCommand((TerminalCommandMetadata) metadata, streamHandler);
    }
}
