package com.helperlib.os;

public class WindowsConfigStrategy implements OSConfigStrategy {

    @Override
    public String getAppFilePath() {
        return System.getenv("APPDATA") + "\\helper-lib\\commands.json";
    }
}