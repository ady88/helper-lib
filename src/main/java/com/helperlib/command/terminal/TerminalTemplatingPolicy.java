package com.helperlib.command.terminal;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.TemplateEngine;
import com.helperlib.api.command.TemplatingPolicy;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Terminal policy: only commandText is templatable.
 */
public final class TerminalTemplatingPolicy implements TemplatingPolicy {

    @Override
    public CommandType supportedType() {
        return CommandType.TERMINAL;
    }

    @Override
    public Set<String> requiredPlaceholders(CommandMetadata metadata, TemplateEngine engine) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(engine, "engine");

        TerminalCommandMetadata m = (TerminalCommandMetadata) metadata;
        return engine.extractPlaceholderNames(m.getCommandText());
    }

    @Override
    public CommandMetadata render(CommandMetadata metadata, Map<String, String> parameters, TemplateEngine engine) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(engine, "engine");

        TerminalCommandMetadata m = (TerminalCommandMetadata) metadata;

        String renderedCmd = engine.render(m.getCommandText(), parameters);

        return new TerminalCommandMetadata(
                m.getName(),
                m.getDescription(),
                m.getType(), // keep type (TERMINAL)
                renderedCmd,
                m.getArguments(),
                m.getPath(),
                m.getEnvironmentPathVariable()
        );
    }
}
