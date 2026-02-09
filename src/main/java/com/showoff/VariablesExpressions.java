package com.showoff;

public final class VariablesExpressions {
    private VariablesExpressions() {}

    public static Results compute() {
        // Primitive variables
        byte b = 10;
        short s = 300;
        int i = 20_000;
        long l = 3_000_000_000L;
        float f = 3.14f;
        double d = 2.718281828;
        char c = 'A';
        boolean flag = true;

        // Reference variables
        String text = "Java";
        String nullRef = null;

        // Arithmetic
        int sum = add(i, b);
        int diff = subtract(i, s);
        long prod = multiply(l, 2);
        double div = divide(d, f);
        int mod = modulo(i, 7);

        // Unary and increment/decrement
        int neg = negate(i);
        int pre = preIncrement(i);
        int post = postIncrement(i);

        // Compound assignment
        int compound = compoundOps(10);

        // Comparisons and logical operators
        boolean gt = i > s;
        boolean eq = isJava(text);
        boolean and = flag && gt;
        boolean or = flag || false;
        boolean not = !flag;

        // Ternary
        String ternary = (i > 10) ? "big" : "small";

        // Bitwise
        int bitAnd = 0b1010 & 0b1100;
        int bitOr = 0b1010 | 0b1100;
        int bitXor = 0b1010 ^ 0b1100;
        int bitNot = ~0b0000_1111;
        int shiftLeft = 1 << 3;
        int shiftRight = 16 >> 2;
        int unsignedShift = -1 >>> 1;

        // Casting and type conversion
        int casted = (int) d;
        double widened = i;
        char fromInt = (char) 66;

        // String concatenation
        String concat = text + " " + c + " " + sum;

        // Overflow example
        int max = Integer.MAX_VALUE;
        int overflow = addOverflow(max);

        // Null check
        boolean isNull = isNull(nullRef);

        return new Results(
            b, s, i, l, f, d, c, flag,
            sum, diff, prod, div, mod,
            neg, pre, post,
            compound,
            gt, eq, and, or, not,
            ternary,
            bitAnd, bitOr, bitXor, bitNot,
            shiftLeft, shiftRight, unsignedShift,
            casted, widened, fromInt,
            concat,
            max, overflow,
            isNull
        );
    }

    public static void printDemo() {
        Results r = compute();
        System.out.println("b=" + r.b + ", s=" + r.s + ", i=" + r.i + ", l=" + r.l);
        System.out.println("f=" + r.f + ", d=" + r.d + ", c=" + r.c + ", flag=" + r.flag);
        System.out.println("sum=" + r.sum + ", diff=" + r.diff + ", prod=" + r.prod + ", div=" + r.div + ", mod=" + r.mod);
        System.out.println("neg=" + r.neg + ", pre=" + r.pre + ", post=" + r.post);
        System.out.println("compound=" + r.compound);
        System.out.println("gt=" + r.gt + ", eq=" + r.eq + ", and=" + r.and + ", or=" + r.or + ", not=" + r.not);
        System.out.println("ternary=" + r.ternary);
        System.out.println("bitAnd=" + r.bitAnd + ", bitOr=" + r.bitOr + ", bitXor=" + r.bitXor + ", bitNot=" + r.bitNot);
        System.out.println("shiftLeft=" + r.shiftLeft + ", shiftRight=" + r.shiftRight + ", unsignedShift=" + r.unsignedShift);
        System.out.println("casted=" + r.casted + ", widened=" + r.widened + ", fromInt=" + r.fromInt);
        System.out.println("concat=" + r.concat);
        System.out.println("max=" + r.max + ", overflow=" + r.overflow);
        System.out.println("isNull=" + r.isNull);
    }

    public record Results(
        byte b, short s, int i, long l, float f, double d, char c, boolean flag,
        int sum, int diff, long prod, double div, int mod,
        int neg, int pre, int post,
        int compound,
        boolean gt, boolean eq, boolean and, boolean or, boolean not,
        String ternary,
        int bitAnd, int bitOr, int bitXor, int bitNot,
        int shiftLeft, int shiftRight, int unsignedShift,
        int casted, double widened, char fromInt,
        String concat,
        int max, int overflow,
        boolean isNull
    ) {}

    static int add(int a, int b) {
        return a + b;
    }

    static int subtract(int a, int b) {
        return a - b;
    }

    static long multiply(long a, int b) {
        return a * b;
    }

    static double divide(double a, float b) {
        return a / b;
    }

    static int modulo(int a, int b) {
        return a % b;
    }

    static int negate(int a) {
        return -a;
    }

    static int preIncrement(int a) {
        return ++a;
    }

    static int postIncrement(int a) {
        return a++;
    }

    static int compoundOps(int value) {
        value += 5;
        value *= 2;
        value -= 3;
        value /= 4;
        value %= 5;
        return value;
    }

    static boolean isJava(String text) {
        return "Java".equals(text);
    }

    static int addOverflow(int value) {
        return value + 1;
    }

    static boolean isNull(Object o) {
        return o == null;
    }
}
