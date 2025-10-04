package com.helperlib.command.clipboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Utility service for clipboard operations.
 * Provides high-performance, reusable clipboard functionality for all command types.
 */
public class ClipboardService {

    // Private constructor to prevent instantiation - this is a utility class
    private ClipboardService() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Gets the system clipboard, handling headless environments gracefully.
     *
     * @return Clipboard instance or null if not available
     */
    private static Clipboard getSystemClipboard() {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                return null;
            }
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (HeadlessException e) {
            return null;
        }
    }

    /**
     * Copies the specified text to the system clipboard.
     *
     * @param content the text to copy to clipboard
     * @return ClipboardResult indicating success/failure and timing information
     */
    public static ClipboardResult copyToClipboard(String content) {
        long startTime = System.nanoTime();

        try {
            // Validate input
            if (content == null) {
                return new ClipboardResult(false, "Cannot copy null content to clipboard",
                        (System.nanoTime() - startTime) / 1_000_000);
            }

            // Check for headless environment
            if (GraphicsEnvironment.isHeadless()) {
                return new ClipboardResult(false, "Cannot access clipboard in headless environment",
                        (System.nanoTime() - startTime) / 1_000_000);
            }

            // Get clipboard lazily
            Clipboard clipboard = getSystemClipboard();
            if (clipboard == null) {
                return new ClipboardResult(false, "Cannot access clipboard in headless environment",
                        (System.nanoTime() - startTime) / 1_000_000);
            }

            // Perform clipboard operation
            StringSelection stringSelection = new StringSelection(content);
            clipboard.setContents(stringSelection, null);

            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            return new ClipboardResult(true, "Text copied to clipboard successfully", executionTime);

        } catch (HeadlessException e) {
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            return new ClipboardResult(false, "Cannot access clipboard in headless environment: " + e.getMessage(), executionTime);
        } catch (SecurityException e) {
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            return new ClipboardResult(false, "Security restriction accessing clipboard: " + e.getMessage(), executionTime);
        } catch (Exception e) {
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            return new ClipboardResult(false, "Failed to copy to clipboard: " + e.getMessage(), executionTime);
        }
    }

    /**
     * Simplified version that matches the existing RestCommand behavior.
     * Copies to clipboard and only logs errors to System.err.
     *
     * @param content the text to copy to clipboard
     * @return true if successful, false otherwise
     */
    public static boolean copyToClipboardSilent(String content) {
        ClipboardResult result = copyToClipboard(content);
        if (!result.success()) {
            System.err.println(result.message());
        }
        return result.success();
    }
}