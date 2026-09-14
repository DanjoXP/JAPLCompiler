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

    private static void handleUpdate() {
        try {
            File jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
            ProcessBuilder pb;
            if (isWindows) {
                File updateBat = new File(jarDir, "update.bat");
                String scriptPath = updateBat.exists() ? updateBat.getAbsolutePath() : "update.bat";
                pb = new ProcessBuilder("cmd.exe", "/c", scriptPath, "--no-pause");
            } else {
                File updateSh = new File(jarDir, "update.sh");
                String scriptPath = updateSh.exists() ? updateSh.getAbsolutePath() : "./update.sh";
                pb = new ProcessBuilder("bash", scriptPath, "--no-pause");
            }
            pb.directory(jarDir);
            pb.inheritIO();
            Process p = pb.start();
            int code = p.waitFor();
            System.exit(code);
        } catch (Exception e) {
            System.err.println("Failed to launch updater: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("=========================================================");
        System.out.println("           Stirlang Programming Language Compiler            ");
        System.out.println("=========================================================");
        System.out.println("Usage:");
        System.out.println("  Stirlang <file.stirl>                              Compile and run");
        System.out.println("  Stirlang run <file.stirl>                          Compile and run");
        System.out.println("  Stirlang compile <file.stirl>                      Compile to .java and .class");
        System.out.println("  Stirlang java <file.stirl>                         Inspect generated Java source");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -v, --version           Display version information");
        System.out.println("  -update, --update       Check and install latest updates from GitHub");
        System.out.println("  -o, --out <directory>   Output directory for generated files (default: ./build)");
        System.out.println("  -h, --help              Display this help menu");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  Stirlang examples/hello.stirl");
        System.out.println("  Stirlang compile examples/adult_check.stirl -o dist");
        System.out.println("  Stirlang java examples/math_operations.stirl");
        System.out.println("  Stirlang -update");
    }
}
