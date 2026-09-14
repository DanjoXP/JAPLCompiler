package com.stirlang.test;

import com.stirlang.ast.ProgramNode;
import com.stirlang.common.SemanticException;
import com.stirlang.lexer.Lexer;
import com.stirlang.lexer.Token;
import com.stirlang.parser.Parser;
import com.stirlang.semantic.SemanticAnalyzer;

import java.util.List;

import static com.stirlang.test.StirlangTestRunner.*;

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
        }, "Should throw SemanticException for missing entry point");

        assertTrue(ex.getMessage().contains("No Access Point"), "Error message should mention No Access Point");
    }

    public void testTopLevelFunctionCallWithoutMain() {
        String code = "Begin Function greet(name)\nprint(name)\nEnd Function\ngreet(\"Danny\")";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        // Should not throw any exception because top-level call acts as access point!
        analyzer.analyze(program);
        assertTrue(program.getMainFunction() != null, "Synthetic main should be created");
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
