package com.japl;

import com.japl.common.CompilerException;
import com.japl.compiler.JAPLCompiler;
import com.japl.runner.JavaCompilerService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Command-Line Interface (CLI) entry point for the JAPL compiler.
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
            System.out.println("JAPL Compiler version " + VERSION);
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
            System.err.println("Error: No .japl input file specified.");
            printHelp();
            System.exit(1);
        }

        File inputFile = new File(filePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            // Check if Notepad appended .txt (e.g. program.japl.txt)
            File txtFile = new File(filePath + ".txt");
            if (txtFile.exists() && txtFile.isFile()) {
                System.out.println("Notice: Using '" + txtFile.getName() + "' (detected .txt extension added by editor).");
                inputFile = txtFile;
            } else {
                System.err.println("Error: Input file not found: " + filePath);
                System.exit(1);
            }
        }

        JAPLCompiler compiler = new JAPLCompiler();

        try {
            switch (command) {
                case "java": {
                    String source = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
                    String className = JAPLCompiler.sanitizeClassName(JAPLCompiler.getBaseName(inputFile.getName()));
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
                    int exitCode = compiler.run(inputFile, outputDir);
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

    private static void printHelp() {
        System.out.println("=========================================================");
        System.out.println("           JAPL Programming Language Compiler            ");
        System.out.println("=========================================================");
        System.out.println("Usage:");
        System.out.println("  java -jar japl.jar <file.japl>                Compile and run");
        System.out.println("  java -jar japl.jar run <file.japl>            Compile and run");
        System.out.println("  java -jar japl.jar compile <file.japl>        Compile to .java and .class");
        System.out.println("  java -jar japl.jar java <file.japl>           Inspect generated Java source");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -o, --out <directory>   Output directory for generated files (default: ./build)");
        System.out.println("  -v, --version           Display version information");
        System.out.println("  -h, --help              Display this help menu");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar japl.jar examples/hello.japl");
        System.out.println("  java -jar japl.jar compile examples/adult_check.japl -o dist");
        System.out.println("  java -jar japl.jar java examples/math_operations.japl");
    }
}
