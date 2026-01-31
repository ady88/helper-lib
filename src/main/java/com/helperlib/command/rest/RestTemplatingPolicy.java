package com.helperlib.command.rest;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.TemplateEngine;
import com.helperlib.api.command.TemplatingPolicy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * REST policy: url, requestBody, and header values are templatable.
 */
public final class RestTemplatingPolicy implements TemplatingPolicy {

    @Override
    public CommandType supportedType() {
        return CommandType.REST;
    }

    @Override
    public Set<String> requiredPlaceholders(CommandMetadata metadata, TemplateEngine engine) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(engine, "engine");

        RestCommandMetadata m = (RestCommandMetadata) metadata;

        Set<String> names = new HashSet<>();
        names.addAll(engine.extractPlaceholderNames(m.getUrl()));
        names.addAll(engine.extractPlaceholderNames(m.getRequestBody()));

        if (m.getHeaders() != null && !m.getHeaders().isEmpty()) {
            for (String headerValue : m.getHeaders().values()) {
                names.addAll(engine.extractPlaceholderNames(headerValue));
            }
        }

        return Set.copyOf(names);
    }

    @Override
    public CommandMetadata render(CommandMetadata metadata, Map<String, String> parameters, TemplateEngine engine) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(engine, "engine");

        RestCommandMetadata m = (RestCommandMetadata) metadata;

        String renderedUrl = engine.render(m.getUrl(), parameters);
        String renderedBody = engine.render(m.getRequestBody(), parameters);

        Map<String, String> renderedHeaders = null;
        if (m.getHeaders() != null && !m.getHeaders().isEmpty()) {
            renderedHeaders = new HashMap<>(m.getHeaders().size());
            for (Map.Entry<String, String> e : m.getHeaders().entrySet()) {
                renderedHeaders.put(e.getKey(), engine.render(e.getValue(), parameters));
            }
        } else if (m.getHeaders() != null) {
            renderedHeaders = Map.of();
        }

        return new RestCommandMetadata(
                m.getName(),
                m.getDescription(),
                renderedUrl,
                m.getMethod(),
                renderedBody,
                renderedHeaders,
                m.getToClipboard(),
                m.isShowResultImmediately()
        );
    }
}