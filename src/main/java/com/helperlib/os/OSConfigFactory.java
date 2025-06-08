package com.helperlib.os;


public class OSConfigFactory {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_TEST_ENV = Boolean.parseBoolean(System.getenv("IS_TEST"));

    /**
     * Returns the appropriate OSConfigStrategy based on the current operating system
     * and the test environment flag.
     *
     * @return The OSConfigStrategy implementation
     */
    public static OSConfigStrategy getOSConfigStrategy() {
        boolean isTestEnv = Boolean.parseBoolean(System.getenv("IS_TEST")) ||
                Boolean.parseBoolean(System.getProperty("IS_TEST", "false")); // Check both environment variable and system property


        if (OS_NAME.contains("win")) {
            return isTestEnv ? new WindowsTestConfigStrategy() : new WindowsConfigStrategy();
        } else if (OS_NAME.contains("mac")) {
            return isTestEnv ? new MacOSTestConfigStrategy() : new MacOSConfigStrategy();
        } else if (OS_NAME.contains("nix") || OS_NAME.contains("nux")) {
            return isTestEnv ? new LinuxTestConfigStrategy() : new LinuxConfigStrategy();
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + OS_NAME);
        }
    }
}
