package com.helperlib.command.rest;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.logging.StreamHandler;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory implementation for RestCommands.
 */
public class RestCommandFactory implements CommandFactory {

    @Override
    public CommandMetadata parseMetadata(JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        String description = jsonObject.getString("description");
        String url = jsonObject.getString("url");
        String method = jsonObject.getString("method", "GET");
        String requestBody = jsonObject.getString("requestBody", "");
        String toClipboard = jsonObject.getString("toClipboard", "");
        boolean showResultImmediately = jsonObject.getBoolean("showResultImmediately", false);

        // Parse headers
        Map<String, String> headers = new HashMap<>();
        if (jsonObject.containsKey("headers")) {
            JsonObject headersJson = jsonObject.getJsonObject("headers");
            headersJson.forEach((key, value) ->
                    headers.put(key, value.toString().replaceAll("\"", "")));
        }

        RestCommandMetadata metadata = new RestCommandMetadata(
                name, description, url, method, requestBody, headers, toClipboard, showResultImmediately
        );

        // New: parse captureToParameters (paramName -> jsonPath)
        if (jsonObject.containsKey("captureToParameters")) {
            JsonObject captureJson = jsonObject.getJsonObject("captureToParameters");
            Map<String, String> capture = new HashMap<>();
            captureJson.forEach((k, v) -> capture.put(k, v.toString().replaceAll("\"", "")));
            metadata.setCaptureToParameters(capture);
        }

        return metadata;
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata metadata) {
        RestCommandMetadata restMetadata = (RestCommandMetadata) metadata;

        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("name", restMetadata.getName())
                .add("description", restMetadata.getDescription())
                .add("type", CommandType.REST.toString())
                .add("url", restMetadata.getUrl())
                .add("method", restMetadata.getMethod())
                .add("requestBody", restMetadata.getRequestBody() != null ? restMetadata.getRequestBody() : "")
                .add("toClipboard", restMetadata.getToClipboard() != null ? restMetadata.getToClipboard() : "")
                .add("showResultImmediately", restMetadata.isShowResultImmediately());

        // Add headers
        if (restMetadata.getHeaders() != null && !restMetadata.getHeaders().isEmpty()) {
            JsonObjectBuilder headersBuilder = Json.createObjectBuilder();
            restMetadata.getHeaders().forEach(headersBuilder::add);
            builder.add("headers", headersBuilder.build());
        }

        // New: serialize captureToParameters if present
        if (restMetadata.getCaptureToParameters() != null && !restMetadata.getCaptureToParameters().isEmpty()) {
            JsonObjectBuilder captureBuilder = Json.createObjectBuilder();
            restMetadata.getCaptureToParameters().forEach(captureBuilder::add);
            builder.add("captureToParameters", captureBuilder.build());
        }

        return builder.build();
    }

    @Override
    public Command createCommand(CommandMetadata metadata, StreamHandler streamHandler) {
        return new RestCommand((RestCommandMetadata) metadata, streamHandler);
    }
}