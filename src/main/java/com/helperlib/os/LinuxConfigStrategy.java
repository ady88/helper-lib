package com.helperlib.os;

public class LinuxConfigStrategy implements OSConfigStrategy {

    @Override
    public String getAppFilePath() {
        return System.getProperty("user.home") + "/.local/share/helper-lib/commands.json";
    }
}