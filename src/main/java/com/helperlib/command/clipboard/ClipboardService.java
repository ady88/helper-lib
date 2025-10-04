
package com.helperlib.command.clipboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Utility service for clipboard operations.
 * Provides high-performance, reusable clipboard functionality for all command types.
 */
public class ClipboardService {

    // Cache the system clipboard to avoid repeated lookups
    private static final Clipboard SYSTEM_CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    // Private constructor to prevent instantiation - this is a utility class
    private ClipboardService() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
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

            // Perform clipboard operation
            StringSelection stringSelection = new StringSelection(content);
            SYSTEM_CLIPBOARD.setContents(stringSelection, null);

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