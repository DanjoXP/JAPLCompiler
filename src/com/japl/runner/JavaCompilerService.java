package com.japl.runner;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for compiling generated Java source code into bytecode (.class files).
 * Uses javax.tools.JavaCompiler API when available, and seamlessly falls back to the javac CLI.
 */
public class JavaCompilerService {

    public static class CompilationResult {
        private final boolean success;
        private final File javaFile;
        private final File outputDirectory;
        private final String className;
        private final String errorMessage;

        public CompilationResult(boolean success, File javaFile, File outputDirectory, String className, String errorMessage) {
            this.success = success;
            this.javaFile = javaFile;
            this.outputDirectory = outputDirectory;
            this.className = className;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public File getJavaFile() {
            return javaFile;
        }

        public File getOutputDirectory() {
            return outputDirectory;
        }

        public String getClassName() {
            return className;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Compiles the given Java source code.
     *
     * @param javaSource Code content
     * @param className  Name of the public class
     * @param outputDir  Directory where .java and .class files are placed
     * @return CompilationResult
     */
    public CompilationResult compile(String javaSource, String className, File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File javaFile = new File(outputDir, className + ".java");
        try {
            Files.writeString(javaFile.toPath(), javaSource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new CompilationResult(false, javaFile, outputDir, className,
                    "Failed to write Java source file: " + e.getMessage());
        }

        // Attempt 1: In-process javax.tools.JavaCompiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler != null) {
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> compilationUnits =
                        fileManager.getJavaFileObjectsFromFiles(List.of(javaFile));
                List<String> options = List.of("-d", outputDir.getAbsolutePath());
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
                boolean success = task.call();

                if (success) {
                    return new CompilationResult(true, javaFile, outputDir, className, null);
                } else {
                    StringBuilder errors = new StringBuilder();
                    for (Diagnostic<? extends JavaFileObject> diag : diagnostics.getDiagnostics()) {
                        errors.append(diag.getMessage(null)).append("\n");
                    }
                    return new CompilationResult(false, javaFile, outputDir, className, errors.toString());
                }
            } catch (Exception e) {
                // Fall back to process builder below
            }
        }

        // Attempt 2: Fallback to running 'javac' executable
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "javac",
                    "-encoding", "UTF-8",
                    "-d", outputDir.getAbsolutePath(),
                    javaFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return new CompilationResult(true, javaFile, outputDir, className, null);
            } else {
                return new CompilationResult(false, javaFile, outputDir, className, output);
            }
        } catch (Exception e) {
            return new CompilationResult(false, javaFile, outputDir, className,
                    "Could not invoke Java compiler (javac): " + e.getMessage());
        }
    }
}
