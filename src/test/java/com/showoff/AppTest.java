package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void main_printsHelloWorld_withArgs() {
        String output = captureStdout(() -> App.main(new String[] {"arg1", "arg2"}));
        assertEquals("Hello World" + System.lineSeparator(), output);
    }

    @Test
    void main_printsHelloWorld_withNullArgs() {
        String output = captureStdout(() -> assertDoesNotThrow(() -> App.main(null)));
        assertEquals("Hello World" + System.lineSeparator(), output);
    }

    private static String captureStdout(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(buffer);
        try {
            System.setOut(testOut);
            action.run();
        } finally {
            System.setOut(originalOut);
            testOut.flush();
        }
        return buffer.toString();
    }
}
