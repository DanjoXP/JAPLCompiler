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

    public void testCounterVariableScopedToLoop() {
        String code = "Begin Function main()\n" +
                      "Start Loop(5) as i\n" +
                      "    print(i)\n" +
                      "End Loop\n" +
                      "print(i)\n" +
                      "End Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException when accessing loop counter outside loop");

        assertTrue(ex.getMessage().contains("used before being defined"),
                "Error message should mention variable used before being defined, got: " + ex.getMessage());
    }

    public void testBreakOutsideLoopThrowsException() {
        String code = "Begin Function main()\n" +
                      "Break Loop\n" +
                      "End Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for Break Loop outside loop");

        assertTrue(ex.getMessage().contains("'Break Loop' cannot be used outside of a loop"),
                "Error message should mention Break Loop cannot be used outside of a loop");
    }

    public void testContinueOutsideLoopThrowsException() {
        String code = "Begin Function main()\n" +
                      "Continue Loop\n" +
                      "End Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for Continue Loop outside loop");

        assertTrue(ex.getMessage().contains("'Continue Loop' cannot be used outside of a loop"),
                "Error message should mention Continue Loop cannot be used outside of a loop");
    }

    public void testNonNumericLoopCountThrowsException() {
        String code = "Begin Function main()\n" +
                      "Start Loop(\"not a number\")\n" +
                      "    print(\"Hello\")\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for non-numeric loop count");

        assertTrue(ex.getMessage().contains("Loop count expression must evaluate to a number"),
                "Error message should mention loop count expression must evaluate to a number");
    }

    public void testRedeclaringVariableAsCounterThrowsException() {
        String code = "Begin Function main()\n" +
                      "i = 10\n" +
                      "Start Loop(5) as i\n" +
                      "    print(i)\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for declaring counter matching existing variable");

        assertTrue(ex.getMessage().contains("already declared in an enclosing scope"),
                "Error message should mention variable already declared in an enclosing scope");
    }
}
