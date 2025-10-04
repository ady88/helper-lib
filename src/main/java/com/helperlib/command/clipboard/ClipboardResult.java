package com.helperlib.command.clipboard;

/**
 * Result object for clipboard operations containing success status,
 * message, and execution timing information.
 */
public record ClipboardResult(boolean success, String message, long executionTimeMs) {

    @Override
    public String toString() {
        return String.format("ClipboardResult{success=%s, message='%s', executionTimeMs=%d}",
                success, message, executionTimeMs);
    }
}