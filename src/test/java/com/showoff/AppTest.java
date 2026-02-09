package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void main_printsHelloWorld() {
        String output = captureStdout(() -> App.main(new String[0]));
        assertEquals("Hello World" + System.lineSeparator(), output);
    }

    @Test
    void compute_returnsExpectedValues() {
        VariablesExpressions.Results r = VariablesExpressions.compute();

        assertEquals(10, r.b());
        assertEquals(300, r.s());
        assertEquals(20_000, r.i());
        assertEquals(3_000_000_000L, r.l());
        assertEquals(3.14f, r.f());
        assertEquals(2.718281828, r.d());
        assertEquals('A', r.c());
        assertTrue(r.flag());

        assertEquals(20_010, r.sum());
        assertEquals(19_700, r.diff());
        assertEquals(6_000_000_000L, r.prod());
        assertEquals(2.718281828 / 3.14f, r.div());
        assertEquals(20_000 % 7, r.mod());

        assertEquals(-20_000, r.neg());
        assertEquals(20_001, r.pre());
        assertEquals(20_000, r.post());

        assertEquals(1, r.compound());

        assertTrue(r.gt());
        assertTrue(r.eq());
        assertTrue(r.and());
        assertTrue(r.or());
        assertFalse(r.not());

        assertEquals("big", r.ternary());
        assertEquals(0b1000, r.bitAnd());
        assertEquals(0b1110, r.bitOr());
        assertEquals(0b0110, r.bitXor());
        assertEquals(~0b0000_1111, r.bitNot());
        assertEquals(8, r.shiftLeft());
        assertEquals(4, r.shiftRight());
        assertEquals(-1 >>> 1, r.unsignedShift());

        assertEquals(2, r.casted());
        assertEquals(20_000d, r.widened());
        assertEquals('B', r.fromInt());

        assertEquals("Java A 20010", r.concat());
        assertEquals(Integer.MAX_VALUE, r.max());
        assertEquals(Integer.MIN_VALUE, r.overflow());
        assertTrue(r.isNull());
    }

    @Test
    void isJava_handlesNullAndNonMatching() {
        assertFalse(VariablesExpressions.isJava(null));
        assertFalse(VariablesExpressions.isJava("Kotlin"));
        assertTrue(VariablesExpressions.isJava("Java"));
    }

    @Test
    void modulo_throwsOnZeroDivisor() {
        assertThrows(ArithmeticException.class, () -> VariablesExpressions.modulo(10, 0));
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
