package com.stirlang.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight, zero-dependency unit testing framework for Stirlang Compiler components.
 */
public class StirlangTestRunner {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("                Running Stirlang Test Suite                 ");
        System.out.println("=========================================================\n");

        runSuite(LexerTest.class);
        runSuite(ParserTest.class);
        runSuite(SemanticAnalyzerTest.class);
        runSuite(EndToEndTest.class);

        System.out.println("\n=========================================================");
        System.out.println(String.format("Tests run: %d | Passed: %d | Failed: %d", totalTests, passedTests, failedTests));
        if (failedTests > 0) {
            System.out.println("\nFailures:");
            for (String f : failures) {
                System.out.println("  * " + f);
            }
            System.out.println("=========================================================");
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY!");
            System.out.println("=========================================================");
        }
    }

    public static void runSuite(Class<?> testClass) {
        System.out.println("Running " + testClass.getSimpleName() + "...");
        Object instance;
        try {
            instance = testClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Could not instantiate " + testClass.getName() + ": " + e.getMessage());
            return;
        }

        for (Method method : testClass.getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                totalTests++;
                try {
                    method.invoke(instance);
                    passedTests++;
                    System.out.println("  [PASS] " + method.getName());
                } catch (Exception e) {
                    failedTests++;
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String failureMsg = testClass.getSimpleName() + "." + method.getName() + ": " + cause.getMessage();
                    failures.add(failureMsg);
                    System.out.println("  [FAIL] " + method.getName() + " -> " + cause.getMessage());
                }
            }
        }
    }

    // ==========================================
    // Assertion Utilities
    // ==========================================

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " (Expected: <" + expected + ">, Actual: <" + actual + ">)");
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "Values are not equal");
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, "Condition expected to be true");
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, "Condition expected to be false");
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    public static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError(message + " (Expected null, got: " + obj + ")");
        }
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return expectedType.cast(t);
            }
            throw new AssertionError(message + ": Expected exception of type " + expectedType.getName() +
                    " but caught " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(message + ": Expected exception " + expectedType.getName() + " was not thrown.");
    }
}
