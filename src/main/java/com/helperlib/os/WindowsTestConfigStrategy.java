package com.helperlib.os;

public class WindowsTestConfigStrategy implements OSConfigStrategy {

    @Override
    public String getAppFilePath() {
        return System.getenv("APPDATA") + "\\helper-lib\\commands-test.json";
    }
}

