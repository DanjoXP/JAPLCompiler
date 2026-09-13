package com.japl.test;

import com.japl.ast.ProgramNode;
import com.japl.common.SemanticException;
import com.japl.lexer.Lexer;
import com.japl.lexer.Token;
import com.japl.parser.Parser;
import com.japl.semantic.SemanticAnalyzer;

import java.util.List;

import static com.japl.test.JAPLTestRunner.*;

public class SemanticAnalyzerTest {

    private ProgramNode parse(String code, Lexer[] lexerRef) {
        Lexer lexer = new Lexer(code);
        if (lexerRef != null && lexerRef.length > 0) {
            lexerRef[0] = lexer;
        }
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        return parser.parse();
    }

    public void testMissingMainThrowsException() {
        String code = "Begin Function greet(name)\nprint(name)\nEnd Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for missing main");

        assertTrue(ex.getMessage().contains("missing entry point"), "Error message should mention missing entry point");
    }

    public void testDuplicateFunctionThrowsException() {
        String code = "Begin Function main()\nEnd Function\nBegin Function main()\nEnd Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for duplicate function");

        assertTrue(ex.getMessage().contains("Duplicate function"), "Error message should mention Duplicate function");
    }

    public void testUndeclaredVariableThrowsException() {
        String code = "Begin Function main()\nprint(age)\nEnd Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for undeclared variable");

        assertTrue(ex.getMessage().contains("used before being defined"), "Error message should mention used before defined");
    }

    public void testTypeMismatchThrowsException() {
        String code = "Begin Function main()\nage = 30\nage = \"Thirty\"\nEnd Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for type mismatch");

        assertTrue(ex.getMessage().contains("Cannot assign value of type STRING to variable 'age' of type INT"),
                "Should report incompatible assignment");
    }

    public void testCompoundAssignmentOnUndeclaredThrowsException() {
        String code = "Begin Function main()\ntotal += 10\nEnd Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for compound assignment on undeclared variable");

        assertTrue(ex.getMessage().contains("Cannot use compound assignment"), "Error message should mention compound assignment");
    }
}
