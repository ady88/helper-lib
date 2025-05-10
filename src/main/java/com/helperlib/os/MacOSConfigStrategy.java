package com.helperlib.os;

public class MacOSConfigStrategy implements OSConfigStrategy {

    @Override
    public String getAppFilePath() {
        return System.getProperty("user.home") + "/Library/Application Support/helper-lib/commands.json";
    }
}