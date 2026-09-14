package com.stirlang.runner;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Executes a compiled Java class file.
 */
public class ProgramRunner {

    public static class ExecutionResult {
        private final int exitCode;
        private final String output;
        private final String error;

        public ExecutionResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }
    }

    /**
     * Executes the compiled class in the specified directory and captures output.
     */
    public ExecutionResult runAndCapture(File classpathDir, String className, String... args) throws Exception {
        java.util.List<String> command = new java.util.ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(classpathDir.getAbsolutePath());
        command.add(className);
        if (args != null) {
            for (String a : args) {
                command.add(a);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        return new ExecutionResult(exitCode, stdout, stderr);
    }

    /**
     * Executes the compiled class and connects its I/O directly to the current console.
     */
    public int runInheritIO(File classpathDir, String className, String... args) throws Exception {
        java.util.List<String> command = new java.util.ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(classpathDir.getAbsolutePath());
        command.add(className);
        if (args != null) {
            for (String a : args) {
                command.add(a);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        return process.waitFor();
    }
}
