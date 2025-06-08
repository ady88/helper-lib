package com.helperlib.os;

/**
 * Test implementation of MacOSConfigStrategy to provide an isolated configuration file for testing.
 */
public class MacOSTestConfigStrategy implements OSConfigStrategy {

    @Override
    public String getAppFilePath() {
        // Use a test-specific file name for the test environment
        return System.getProperty("user.home") + "/Library/Application Support/helper-lib/commands-test.json";
    }
}

