package com.showoff;

import java.util.ArrayList;
import java.util.List;

public final class ControlFlow {
    private ControlFlow() {}

    public static String classifyNumber(int n) {
        if (n < 0) {
            return "negative";
        } else if (n == 0) {
            return "zero";
        } else {
            return "positive";
        }
    }

    public static String dayType(int day) {
        return switch (day) {
            case 1, 2, 3, 4, 5 -> "weekday";
            case 6, 7 -> "weekend";
            default -> throw new IllegalArgumentException("day must be 1..7");
        };
    }

    public static int sumFirstN(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        long result = 1;
        int i = n;
        while (i > 1) {
            result *= i;
            i--;
        }
        return result;
    }

    public static int countDigits(int n) {
        int value = Math.abs(n);
        int count = 0;
        do {
            count++;
            value /= 10;
        } while (value > 0);
        return count;
    }

    public static String joinWithDash(String[] items) {
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (item == null) {
                throw new IllegalArgumentException("items must not contain null");
            }
            if (!sb.isEmpty()) {
                sb.append('-');
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static int sumSkippingMultiples(int limit, int skipMultiple) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }
        if (skipMultiple <= 0) {
            throw new IllegalArgumentException("skipMultiple must be > 0");
        }
        int sum = 0;
        for (int i = 1; i <= limit; i++) {
            if (i % skipMultiple == 0) {
                continue;
            }
            if (sum > 1_000_000) {
                break;
            }
            sum += i;
        }
        return sum;
    }

    public static int multiplicationTableSum(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be >= 0");
        }
        int sum = 0;
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                sum += i * j;
            }
        }
        return sum;
    }

    public static int parseIntOrDefault(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static List<Integer> filterEven(List<Integer> numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("numbers must not be null");
        }
        List<Integer> evens = new ArrayList<>();
        for (int n : numbers) {
            if (n % 2 != 0) {
                continue;
            }
            evens.add(n);
        }
        return evens;
    }
}
