package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class ControlFlowTest {
    @Test
    void classifyNumber_coversAllBranches() {
        assertEquals("negative", ControlFlow.classifyNumber(-1));
        assertEquals("zero", ControlFlow.classifyNumber(0));
        assertEquals("positive", ControlFlow.classifyNumber(7));
    }

    @Test
    void dayType_switchCases() {
        assertEquals("weekday", ControlFlow.dayType(1));
        assertEquals("weekday", ControlFlow.dayType(5));
        assertEquals("weekend", ControlFlow.dayType(6));
        assertEquals("weekend", ControlFlow.dayType(7));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.dayType(0));
    }

    @Test
    void sumFirstN_forLoop() {
        assertEquals(0, ControlFlow.sumFirstN(0));
        assertEquals(6, ControlFlow.sumFirstN(3));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.sumFirstN(-1));
    }

    @Test
    void factorial_whileLoop() {
        assertEquals(1, ControlFlow.factorial(0));
        assertEquals(1, ControlFlow.factorial(1));
        assertEquals(120, ControlFlow.factorial(5));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.factorial(-1));
    }

    @Test
    void countDigits_doWhileLoop() {
        assertEquals(1, ControlFlow.countDigits(0));
        assertEquals(1, ControlFlow.countDigits(7));
        assertEquals(3, ControlFlow.countDigits(999));
        assertEquals(4, ControlFlow.countDigits(-1234));
    }

    @Test
    void joinWithDash_forEachLoop() {
        assertEquals("", ControlFlow.joinWithDash(new String[0]));
        assertEquals("a", ControlFlow.joinWithDash(new String[] {"a"}));
        assertEquals("a-b", ControlFlow.joinWithDash(new String[] {"a", "b"}));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.joinWithDash(null));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.joinWithDash(new String[] {"a", null}));
    }

    @Test
    void sumSkippingMultiples_breakAndContinue() {
        assertEquals(0, ControlFlow.sumSkippingMultiples(0, 2));
        assertEquals(9, ControlFlow.sumSkippingMultiples(5, 2));
        assertEquals(1_001_059, ControlFlow.sumSkippingMultiples(2_000, 7));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.sumSkippingMultiples(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.sumSkippingMultiples(5, 0));
    }

    @Test
    void multiplicationTableSum_nestedLoops() {
        assertEquals(0, ControlFlow.multiplicationTableSum(0));
        assertEquals(1, ControlFlow.multiplicationTableSum(1));
        assertEquals(9, ControlFlow.multiplicationTableSum(2));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.multiplicationTableSum(-1));
    }

    @Test
    void parseIntOrDefault_tryCatch() {
        assertEquals(123, ControlFlow.parseIntOrDefault("123", 0));
        assertEquals(7, ControlFlow.parseIntOrDefault("not-a-number", 7));
    }

    @Test
    void filterEven_continueBranch() {
        assertIterableEquals(List.of(), ControlFlow.filterEven(List.of()));
        assertIterableEquals(List.of(2, 4), ControlFlow.filterEven(List.of(1, 2, 3, 4, 5)));
        assertThrows(IllegalArgumentException.class, () -> ControlFlow.filterEven(null));
    }
}
