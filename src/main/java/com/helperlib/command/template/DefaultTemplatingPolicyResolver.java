package com.helperlib.command.template;

import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.TemplatingPolicy;
import com.helperlib.api.command.TemplatingPolicyResolver;
import com.helperlib.api.command.NoOpTemplatingPolicy;
import com.helperlib.command.rest.RestTemplatingPolicy;
import com.helperlib.command.terminal.TerminalTemplatingPolicy;
import com.helperlib.command.terminaltoggle.TerminalToggleTemplatingPolicy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default mapping from CommandType -> TemplatingPolicy.
 * Unknown/unsupported types fall back to a NoOpTemplatingPolicy.
 */
public final class DefaultTemplatingPolicyResolver implements TemplatingPolicyResolver {

    private final Map<CommandType, TemplatingPolicy> policies;

    public DefaultTemplatingPolicyResolver() {
        EnumMap<CommandType, TemplatingPolicy> map = new EnumMap<>(CommandType.class);

        // Supported templating types
        map.put(CommandType.REST, new RestTemplatingPolicy());
        map.put(CommandType.TERMINAL, new TerminalTemplatingPolicy());
        map.put(CommandType.TERMINAL_TOGGLE, new TerminalToggleTemplatingPolicy());

        this.policies = Map.copyOf(map);
    }

    @Override
    public TemplatingPolicy resolve(CommandType type) {
        Objects.requireNonNull(type, "type");
        return policies.getOrDefault(type, new NoOpTemplatingPolicy(type));
    }
}
