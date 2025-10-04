
package com.helperlib.command.rest;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandResult;
import com.helperlib.api.command.logging.StreamHandler;
import com.helperlib.command.clipboard.ClipboardService;
import com.helperlib.core.command.CommandExecutorService;
import com.helperlib.core.command.logging.NoOpStreamHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;

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
                // Create HTTP client
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(30))
                        .build();

                // Build request
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(restMetadata.getUrl()))
                        .timeout(Duration.ofSeconds(60));

                // Add headers
                if (restMetadata.getHeaders() != null) {
                    restMetadata.getHeaders().forEach(requestBuilder::header);
                }

                // Set HTTP method and body
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

                // Execute request
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                // Send full response to StreamHandler
                if (streamHandler != null) {
                    String logMessage = String.format("REST %s %s - Status: %d\n%s",
                            restMetadata.getMethod(), restMetadata.getUrl(), response.statusCode(), responseBody);
                    streamHandler.handleStream(
                            new java.io.ByteArrayInputStream(logMessage.getBytes()),
                            "stdout",
                            restMetadata.getName()
                    );
                }

                // Handle clipboard functionality using the shared service
                String clipboardContent = responseBody; // Default to full response

                if (restMetadata.getToClipboard() != null && !restMetadata.getToClipboard().isEmpty()) {
                    // Extract specific field from JSON response
                    try {
                        clipboardContent = extractJsonPath(responseBody, restMetadata.getToClipboard());
                    } catch (Exception e) {
                        System.err.println("Failed to extract JSON path '" + restMetadata.getToClipboard() +
                                "': " + e.getMessage() + ". Using full response.");
                        clipboardContent = responseBody;
                    }
                }

                // Copy to clipboard using the shared service
                boolean clipboardSuccess = ClipboardService.copyToClipboardSilent(clipboardContent);

                if (clipboardSuccess) {
                    System.out.println("REST command executed successfully. Response copied to clipboard.");
                } else {
                    System.out.println("REST command executed successfully. Failed to copy response to clipboard.");
                }

                long executionTime = System.currentTimeMillis() - startTime;
                boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

                return new CommandResult(success, response.statusCode(), executionTime);

            } catch (Exception e) {
                System.err.println("REST command execution failed: " + e.getMessage());
                long executionTime = System.currentTimeMillis() - startTime;
                return new CommandResult(false, -1, executionTime);
            }
        }, CommandExecutorService.getVirtualThreadExecutor());
    }

    private String extractJsonPath(String jsonResponse, String jsonPath) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse))) {
            JsonObject jsonObject = jsonReader.readObject();

            // Simple JSON path extraction (supports dot notation like "data.field")
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

            // Return the string representation of the extracted value
            if (current instanceof jakarta.json.JsonString) {
                return ((jakarta.json.JsonString) current).getString();
            } else {
                return current.toString();
            }
        }
    }
}