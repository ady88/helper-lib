package com.helperlib;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testApp() {
        assertTrue(true);
    }

    @Test
    public void testExecuteCommandFromConfig() {
        App app = new App();
        String result = app.executeCommandFromConfig("default");
        assertEquals("Hello from Helper-Lib!\n", result);
    }
}
