package com.stirlang.compiler;

import com.stirlang.ast.ProgramNode;
import com.stirlang.codegen.JavaCodeGenerator;
import com.stirlang.common.CompilerException;
import com.stirlang.lexer.Lexer;
import com.stirlang.lexer.Token;
import com.stirlang.parser.Parser;
import com.stirlang.runner.JavaCompilerService;
import com.stirlang.runner.ProgramRunner;
import com.stirlang.semantic.SemanticAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Orchestrator coordinating all stages of the Stirlang compilation pipeline:
 * 1. Read source
 * 2. Lexical analysis (Lexer)
 * 3. Syntactic analysis (Parser)
 * 4. Semantic validation (SemanticAnalyzer)
 * 5. Java Code Generation (JavaCodeGenerator)
 * 6. JVM Bytecode Compilation (JavaCompilerService)
 * 7. Program Execution (ProgramRunner)
 */
public class StirlangCompiler {
    private final JavaCompilerService compilerService = new JavaCompilerService();
    private final ProgramRunner runner = new ProgramRunner();

    /**
     * Translates Stirlang source text into Java source code string.
     */
    public String transpile(String source, String className) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexer);
        analyzer.analyze(program);

        JavaCodeGenerator generator = new JavaCodeGenerator(program, className);
        return generator.generate();
    }

    /**
     * Compiles a .stirl source file into .java and .class files in the specified output directory.
     */
    public JavaCompilerService.CompilationResult compile(File sourceFile, File outputDir) throws IOException {
        String source = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
        String baseName = getBaseName(sourceFile.getName());
        String className = sanitizeClassName(baseName);

        String javaSource = transpile(source, className);
        return compilerService.compile(javaSource, className, outputDir);
    }

    /**
     * Compiles and executes a .stirl source file, inheriting console I/O.
     */
    public int run(File sourceFile, File outputDir) throws Exception {
        JavaCompilerService.CompilationResult result = compile(sourceFile, outputDir);
        if (!result.isSuccess()) {
            System.err.println("Compilation failed:\n" + result.getErrorMessage());
            return 1;
        }

        return runner.runInheritIO(outputDir, result.getClassName());
    }

    /**
     * Compiles and executes a .stirl source file, capturing output as a string.
     */
    public ProgramRunner.ExecutionResult runAndCapture(File sourceFile, File outputDir) throws Exception {
        JavaCompilerService.CompilationResult result = compile(sourceFile, outputDir);
        if (!result.isSuccess()) {
            return new ProgramRunner.ExecutionResult(1, "", result.getErrorMessage());
        }

        return runner.runAndCapture(outputDir, result.getClassName());
    }

    /**
     * Compiles and executes Stirlang source text directly in memory/temp folder and captures output.
     */
    public ProgramRunner.ExecutionResult runSourceAndCapture(String source, String className) throws Exception {
        File tempDir = Files.createTempDirectory("stirlang_run_").toFile();
        try {
            String javaSource = transpile(source, className);
            JavaCompilerService.CompilationResult result = compilerService.compile(javaSource, className, tempDir);
            if (!result.isSuccess()) {
                return new ProgramRunner.ExecutionResult(1, "", result.getErrorMessage());
            }
            return runner.runAndCapture(tempDir, className);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    public static String getBaseName(String fileName) {
        if (fileName.toLowerCase().endsWith(".stirl.txt")) {
            return fileName.substring(0, fileName.length() - 9);
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    public static String sanitizeClassName(String name) {
        StringBuilder sb = new StringBuilder();
        if (name.isEmpty() || !Character.isJavaIdentifierStart(name.charAt(0))) {
            sb.append("Stirlang_");
        }
        for (char c : name.toCharArray()) {
            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        dir.delete();
    }
}
