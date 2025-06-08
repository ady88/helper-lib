package com.helperlib.os;

public class LinuxTestConfigStrategy implements OSConfigStrategy {

    @Override
    public String getAppFilePath() {
        return System.getProperty("user.home") + "/.local/share/helper-lib/commands-test.json";
    }
}

