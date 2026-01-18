package com.helperlib.command.rest;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;

import java.util.Map;

public class RestCommandMetadata extends CommandMetadata {
    private String url;
    private String method; // GET, POST, PUT, DELETE
    private String requestBody;
    private Map<String, String> headers;
    private String toClipboard; // JSON path for partial response extraction
    private boolean showResultImmediately; // UI hint: surface result as soon as it is available

    public RestCommandMetadata(String name, String description, String url, String method,
                               String requestBody, Map<String, String> headers, String toClipboard) {
        this(name, description, url, method, requestBody, headers, toClipboard, false);
    }

    public RestCommandMetadata(String name, String description, String url, String method,
                               String requestBody, Map<String, String> headers, String toClipboard,
                               boolean showResultImmediately) {
        super(name, description, CommandType.REST);
        this.url = url;
        this.method = method;
        this.requestBody = requestBody;
        this.headers = headers;
        this.toClipboard = toClipboard;
        this.showResultImmediately = showResultImmediately;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getToClipboard() {
        return toClipboard;
    }

    public void setToClipboard(String toClipboard) {
        this.toClipboard = toClipboard;
    }

    public boolean isShowResultImmediately() {
        return showResultImmediately;
    }

    public void setShowResultImmediately(boolean showResultImmediately) {
        this.showResultImmediately = showResultImmediately;
    }
}