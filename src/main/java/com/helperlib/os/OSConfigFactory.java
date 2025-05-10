package com.helperlib.os;


public class OSConfigFactory {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static OSConfigStrategy getOSConfigStrategy() {
        if (OS_NAME.contains("win")) {
            return new WindowsConfigStrategy();
        } else if (OS_NAME.contains("mac")) {
            return new MacOSConfigStrategy();
        } else if (OS_NAME.contains("nix") || OS_NAME.contains("nux")) {
            return new LinuxConfigStrategy();
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + OS_NAME);
        }
    }
}