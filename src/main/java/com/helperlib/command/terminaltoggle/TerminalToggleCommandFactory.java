package com.helperlib.command.terminaltoggle;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.command.terminal.TerminalCommandMetadata;
import com.helperlib.command.terminal.TerminalMetadataParser;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * Factory implementation for TerminalToggleCommands.
 */
public class TerminalToggleCommandFactory implements CommandFactory {

    @Override
    public CommandMetadata parseMetadata(JsonObject jsonObject) {
        // Parse base terminal metadata first
        TerminalCommandMetadata base = TerminalMetadataParser.parseFromJson(jsonObject, CommandType.TERMINAL_TOGGLE);

        // Extract toggleCommandText if present
        String toggleCommandText = jsonObject.containsKey("toggleCommandText")
                ? jsonObject.getString("toggleCommandText", "")
                : "";

        // Return the enriched toggle metadata
        return new TerminalToggleCommandMetadata(
                base.getName(),
                base.getDescription(),
                base.getType(),
                base.getCommandText(),
                toggleCommandText,
                base.getArguments(),
                base.getPath(),
                base.getEnvironmentPathVariable()
        );
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata metadata) {
        // Serialize base fields first
        JsonObject baseJson = TerminalMetadataParser.serializeToJson((TerminalCommandMetadata) metadata);

        JsonObjectBuilder builder = Json.createObjectBuilder();
        baseJson.forEach(builder::add);

        // Add toggleCommandText if available, otherwise empty string for compatibility
        if (metadata instanceof TerminalToggleCommandMetadata toggleMeta) {
            if (toggleMeta.getToggleCommandText() != null) {
                builder.add("toggleCommandText", toggleMeta.getToggleCommandText());
            } else {
                builder.add("toggleCommandText", "");
            }
        } else {
            builder.add("toggleCommandText", "");
        }

        return builder.build();
    }

    @Override
    public Command createCommand(CommandMetadata metadata, StreamHandler streamHandler) {
        // Ensure we have a TerminalToggleCommandMetadata instance
        TerminalToggleCommandMetadata toggleMetadata;
        if (metadata instanceof TerminalToggleCommandMetadata tm) {
            toggleMetadata = tm;
        } else {
            TerminalCommandMetadata base = (TerminalCommandMetadata) metadata;
            toggleMetadata = new TerminalToggleCommandMetadata(
                    base.getName(),
                    base.getDescription(),
                    base.getType(),
                    base.getCommandText(),
                    "", // no toggle text provided
                    base.getArguments(),
                    base.getPath(),
                    base.getEnvironmentPathVariable()
            );
        }

        String commandId = generateCommandId(toggleMetadata);

        return (Command) ToggleCommandRegistry.getOrCreateCommand(commandId,
                () -> new TerminalToggleCommand(toggleMetadata, streamHandler));
    }

    private String generateCommandId(CommandMetadata metadata) {
        TerminalCommandMetadata terminalMetadata = (TerminalCommandMetadata) metadata;

        // Use name + command text + path for uniqueness
        String name = String.valueOf(terminalMetadata.getName());
        String commandText = String.valueOf(terminalMetadata.getCommandText());
        String path = String.valueOf(terminalMetadata.getPath());

        return String.format("%s-%s-%s",
                name,
                commandText.hashCode(),
                path.hashCode());
    }
}