package com.helperlib.command.rest;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.command.clipboard.ClipboardService;
import com.helperlib.core.command.CommandExecutorService;
import com.helperlib.core.command.CommandRegistry;
import com.helperlib.core.command.logging.NoOpStreamHandler;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RestCommand extends Command {

    private final StreamHandler streamHandler;

    public RestCommand(RestCommandMetadata metadata) {
        this(metadata, new NoOpStreamHandler());
    }

    public RestCommand(RestCommandMetadata metadata, StreamHandler streamHandler) {
        super(metadata);
        this.streamHandler = streamHandler;
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            RestCommandMetadata restMetadata = (RestCommandMetadata) metadata;

            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(30))
                        .build();

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(restMetadata.getUrl()))
                        .timeout(Duration.ofSeconds(60));

                if (restMetadata.getHeaders() != null) {
                    restMetadata.getHeaders().forEach(requestBuilder::header);
                }

                switch (restMetadata.getMethod().toUpperCase()) {
                    case "GET":
                        requestBuilder.GET();
                        break;
                    case "POST":
                        if (restMetadata.getRequestBody() != null && !restMetadata.getRequestBody().isEmpty()) {
                            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(restMetadata.getRequestBody()));
                        } else {
                            requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
                        }
                        break;
                    case "PUT":
                        if (restMetadata.getRequestBody() != null && !restMetadata.getRequestBody().isEmpty()) {
                            requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(restMetadata.getRequestBody()));
                        } else {
                            requestBuilder.PUT(HttpRequest.BodyPublishers.noBody());
                        }
                        break;
                    case "DELETE":
                        requestBuilder.DELETE();
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported HTTP method: " + restMetadata.getMethod());
                }

                HttpRequest request = requestBuilder.build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                if (streamHandler != null) {
                    String logMessage = String.format("REST %s %s - Status: %d\n%s",
                            restMetadata.getMethod(), restMetadata.getUrl(), response.statusCode(), responseBody);
                    streamHandler.handleStream(
                            new java.io.ByteArrayInputStream(logMessage.getBytes()),
                            "stdout",
                            restMetadata.getName()
                    );
                }

                String clipboardContent = responseBody;

                if (restMetadata.getToClipboard() != null && !restMetadata.getToClipboard().isEmpty()) {
                    try {
                        clipboardContent = extractJsonPath(responseBody, restMetadata.getToClipboard());
                    } catch (Exception e) {
                        System.err.println("Failed to extract JSON path '" + restMetadata.getToClipboard() +
                                "': " + e.getMessage() + ". Using full response.");
                        clipboardContent = responseBody;
                    }
                }

                boolean clipboardSuccess = ClipboardService.copyToClipboardSilent(clipboardContent);

                if (clipboardSuccess) {
                    System.out.println("REST command executed successfully. Response copied to clipboard.");
                } else {
                    System.out.println("REST command executed successfully. Failed to copy response to clipboard.");
                }

                long executionTime = System.currentTimeMillis() - startTime;
                boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

                if (success) {
                    captureResponseFieldsToGroupParametersIfConfigured(restMetadata, responseBody);
                }

                return new CommandResult(success, response.statusCode(), executionTime);

            } catch (Exception e) {
                System.err.println("REST command execution failed: " + e.getMessage());
                long executionTime = System.currentTimeMillis() - startTime;
                return new CommandResult(false, -1, executionTime);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }

    private void captureResponseFieldsToGroupParametersIfConfigured(RestCommandMetadata restMetadata, String responseBody) {
        Map<String, String> capture = restMetadata.getCaptureToParameters();
        if (capture == null || capture.isEmpty()) {
            return;
        }

        Map<String, String> ctx = restMetadata.getExecutionContext();
        String category = (ctx != null) ? ctx.get("category") : null;
        String group = (ctx != null) ? ctx.get("group") : null;

        if (category == null || group == null) {
            // Executed directly, or registry didn't provide context => no persistence target.
            return;
        }

        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
            jsonObject = jsonReader.readObject();
        } catch (Exception ignored) {
            return; // not a JSON object response
        }

        Map<String, String> toPersist = new HashMap<>();

        for (Map.Entry<String, String> e : capture.entrySet()) {
            String paramName = e.getKey();
            String jsonPath = e.getValue();
            if (paramName == null || paramName.isBlank() || jsonPath == null || jsonPath.isBlank()) {
                continue;
            }

            Optional<String> extracted = extractJsonPathNullable(jsonObject, jsonPath);
            extracted.ifPresent(value -> {
                if (!value.isBlank()) { // per your rule: empty string should not override
                    toPersist.put(paramName, value);
                }
            });
        }

        if (!toPersist.isEmpty()) {
            CommandRegistry.saveGroupParametersToConfig(category, group, toPersist);
        }
    }

    private Optional<String> extractJsonPathNullable(JsonObject root, String jsonPath) {
        String[] pathParts = jsonPath.split("\\.");
        JsonValue current = root;

        for (String part : pathParts) {
            if (!(current instanceof JsonObject currentObj)) {
                return Optional.empty();
            }
            if (!currentObj.containsKey(part)) {
                return Optional.empty();
            }
            current = currentObj.get(part);
            if (current == JsonValue.NULL) {
                return Optional.empty(); // do not overwrite with null
            }
        }

        if (current == null || current == JsonValue.NULL) {
            return Optional.empty();
        }
        if (current.getValueType() == JsonValue.ValueType.STRING) {
            return Optional.of(((jakarta.json.JsonString) current).getString());
        }
        return Optional.of(current.toString());
    }

    private String extractJsonPath(String jsonResponse, String jsonPath) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse))) {
            JsonObject jsonObject = jsonReader.readObject();

            String[] pathParts = jsonPath.split("\\.");
            Object current = jsonObject;

            for (String part : pathParts) {
                if (current instanceof JsonObject currentObj) {
                    if (currentObj.containsKey(part)) {
                        current = currentObj.get(part);
                    } else {
                        throw new IllegalArgumentException("JSON path not found: " + jsonPath);
                    }
                } else {
                    throw new IllegalArgumentException("Cannot navigate JSON path: " + jsonPath);
                }
            }

            if (current instanceof jakarta.json.JsonString) {
                return ((jakarta.json.JsonString) current).getString();
            } else {
                return current.toString();
            }
        }
    }
}