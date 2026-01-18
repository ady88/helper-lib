package com.helperlib.command.rest;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.TemplateEngine;
import com.helperlib.api.command.TemplatingPolicy;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * REST policy: only url and requestBody are templatable.
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
        return Set.copyOf(names);
    }

    @Override
    public CommandMetadata render(CommandMetadata metadata, Map<String, String> parameters, TemplateEngine engine) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(engine, "engine");

        RestCommandMetadata m = (RestCommandMetadata) metadata;

        String renderedUrl = engine.render(m.getUrl(), parameters);
        String renderedBody = engine.render(m.getRequestBody(), parameters);

        // Preserve non-templatable fields
        return new RestCommandMetadata(
                m.getName(),
                m.getDescription(),
                renderedUrl,
                m.getMethod(),
                renderedBody,
                m.getHeaders(),
                m.getToClipboard(),
                m.isShowResultImmediately()
        );
    }
}