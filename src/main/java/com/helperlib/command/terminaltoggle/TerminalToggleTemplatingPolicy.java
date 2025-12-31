package com.helperlib.command.terminaltoggle;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.TemplateEngine;
import com.helperlib.api.command.TemplatingPolicy;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Terminal toggle policy: commandText and toggleCommandText are templatable.
 */
public final class TerminalToggleTemplatingPolicy implements TemplatingPolicy {

    @Override
    public CommandType supportedType() {
        return CommandType.TERMINAL_TOGGLE;
    }

    @Override
    public Set<String> requiredPlaceholders(CommandMetadata metadata, TemplateEngine engine) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(engine, "engine");

        TerminalToggleCommandMetadata m = (TerminalToggleCommandMetadata) metadata;

        Set<String> names = new HashSet<>();
        names.addAll(engine.extractPlaceholderNames(m.getCommandText()));
        names.addAll(engine.extractPlaceholderNames(m.getToggleCommandText()));
        return Set.copyOf(names);
    }

    @Override
    public CommandMetadata render(CommandMetadata metadata, Map<String, String> parameters, TemplateEngine engine) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(engine, "engine");

        TerminalToggleCommandMetadata m = (TerminalToggleCommandMetadata) metadata;

        String renderedCmd = engine.render(m.getCommandText(), parameters);
        String renderedToggle = engine.render(m.getToggleCommandText(), parameters);

        return new TerminalToggleCommandMetadata(
                m.getName(),
                m.getDescription(),
                m.getType(), // keep type (TERMINAL_TOGGLE)
                renderedCmd,
                renderedToggle,
                m.getArguments(),
                m.getPath(),
                m.getEnvironmentPathVariable()
        );
    }
}
