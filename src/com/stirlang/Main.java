package com.stirlang;

import com.stirlang.common.CompilerException;
import com.stirlang.compiler.StirlangCompiler;
import com.stirlang.runner.JavaCompilerService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Command-Line Interface (CLI) entry point for the Stirlang Compiler.
 */
public class Main {
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        if (args.length == 0 || "-h".equals(args[0]) || "--help".equals(args[0])) {
            printHelp();
            System.exit(0);
        }

        String first = args[0].toLowerCase();
        if ("-v".equals(first) || "--version".equals(first) || "-version".equals(first) || "version".equals(first)) {
            System.out.println("Stirlang Compiler version " + VERSION);
            System.exit(0);
        }

        if ("-update".equals(first) || "--update".equals(first) || "update".equals(first)) {
            handleUpdate();
            System.exit(0);
        }

        String command = "run";
        String filePath = null;
        File outputDir = new File("build");

        int i = 0;
        String firstArg = args[0].toLowerCase();
        if (firstArg.equals("compile") || firstArg.equals("run") || firstArg.equals("java")) {
            command = firstArg;
            i = 1;
        }

        for (; i < args.length; i++) {
            if ("-o".equals(args[i]) || "--out".equals(args[i])) {
                if (i + 1 < args.length) {
                    outputDir = new File(args[++i]);
                } else {
                    System.err.println("Error: Missing argument for output directory flag.");
                    System.exit(1);
                }
            } else if (filePath == null) {
                filePath = args[i];
            } else {
                System.err.println("Error: Unexpected argument '" + args[i] + "'.");
                System.exit(1);
            }
        }

        if (filePath == null) {
            System.err.println("Error: No .stirl input file specified.");
            printHelp();
            System.exit(1);
        }

        File inputFile = new File(filePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            // Check if Notepad appended .txt (e.g. program.stirl.txt)
            File txtFile = new File(filePath + ".txt");
            if (txtFile.exists() && txtFile.isFile()) {
                System.out.println("Notice: Using '" + txtFile.getName() + "' (detected .txt extension added by editor).");
                inputFile = txtFile;
            } else {
                System.err.println("Error: Input file not found: " + filePath);
                System.exit(1);
            }
        }

        StirlangCompiler compiler = new StirlangCompiler();

        try {
            switch (command) {
                case "java": {
                    String source = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
                    String className = StirlangCompiler.sanitizeClassName(StirlangCompiler.getBaseName(inputFile.getName()));
                    String javaSource = compiler.transpile(source, className);
                    System.out.println(javaSource);
                    break;
                }
                case "compile": {
                    System.out.println("Compiling " + inputFile.getName() + "...");
                    JavaCompilerService.CompilationResult result = compiler.compile(inputFile, outputDir);
                    if (result.isSuccess()) {
                        System.out.println("Compilation successful!");
                        System.out.println("Java source: " + result.getJavaFile().getAbsolutePath());
                        System.out.println("Class file:  " + new File(outputDir, result.getClassName() + ".class").getAbsolutePath());
                    } else {
                        System.err.println("Compilation failed:\n" + result.getErrorMessage());
                        System.exit(1);
                    }
                    break;
                }
                case "run": {
                    long startNanos = System.nanoTime();
                    int exitCode = compiler.run(inputFile, outputDir);
                    long elapsedNanos = System.nanoTime() - startNanos;
                    if (exitCode == 0) {
                        System.out.println(formatExecutionTime(inputFile.getName(), elapsedNanos));
                    }
                    System.exit(exitCode);
                    break;
                }
                default:
                    System.err.println("Unknown command: " + command);
                    printHelp();
                    System.exit(1);
            }
        } catch (CompilerException e) {
            System.err.println();
            System.err.println(e.getFormattedMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Compiler error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static String formatExecutionTime(String filename, long elapsedNanos) {
        double elapsedMs = elapsedNanos / 1_000_000.0;
        if (elapsedMs < 1000.0) {
            long ms = Math.round(elapsedMs);
            if (ms < 1000) {
                return filename + " executed in " + ms + " " + (ms == 1 ? "millisecond" : "milliseconds");
            }
        }
        double seconds = elapsedMs / 1000.0;
        return filename + " executed in " + String.format(java.util.Locale.US, "%.2f", seconds) + " seconds";
    }

    private static void handleUpdate() {
        try {
            File jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            File gitDir = new File(jarDir, ".git");
            if (gitDir.exists()) {
                System.out.println("Checking for updates via Git...");
                Process gitProcess = new ProcessBuilder("git", "pull").directory(jarDir).inheritIO().start();
                int gitExit = gitProcess.waitFor();
                if (gitExit == 0) {
                    System.out.println("Running install.bat to rebuild compiler...");
                    File installBat = new File(jarDir, "install.bat");
                    if (installBat.exists()) {
                        Process installProcess = new ProcessBuilder("cmd.exe", "/c", installBat.getAbsolutePath(), "--no-pause").directory(jarDir).inheritIO().start();
                        System.exit(installProcess.waitFor());
                    } else {
                        System.out.println("Update complete. Please run install.bat to rebuild.");
                        System.exit(0);
                    }
                } else {
                    System.err.println("Git pull failed with exit code " + gitExit);
                    System.exit(gitExit);
                }
            } else {
                System.out.println("To update Stirlang, download the latest files and run install.bat.");
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("Failed to perform update: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("=========================================================");
        System.out.println("           Stirlang Programming Language Compiler        ");
        System.out.println("=========================================================");
        System.out.println("Usage:");
        System.out.println("  stirlang <file.stirl>                              Compile and run");
        System.out.println("  stirlang run <file.stirl>                          Compile and run");
        System.out.println("  stirlang compile <file.stirl>                      Compile to .java and .class");
        System.out.println("  stirlang java <file.stirl>                         Inspect generated Java source");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -v, --version           Display version information");
        System.out.println("  -update, --update       Check and install latest updates from GitHub");
        System.out.println("  -o, --out <directory>   Output directory for generated files (default: ./build)");
        System.out.println("  -h, --help              Display this help menu");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  stirlang examples/hello.stirl");
        System.out.println("  stirlang compile examples/adult_check.stirl -o dist");
        System.out.println("  stirlang java examples/math_operations.stirl");
        System.out.println("  stirlang -update");
    }
}
