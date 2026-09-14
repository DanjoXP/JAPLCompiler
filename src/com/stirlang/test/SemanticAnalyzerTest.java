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
        String code = "begin function greet(name)\nprint(name)\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for missing entry point");

        assertTrue(ex.getMessage().contains("No Access Point"), "Error message should mention No Access Point");
    }

    public void testTopLevelFunctionCallWithoutMain() {
        String code = "begin function greet(name)\nprint(name)\nend function\ngreet(\"Danny\")";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        // Should not throw any exception because top-level call acts as access point!
        analyzer.analyze(program);
        assertTrue(program.getMainFunction() != null, "Synthetic main should be created");
    }

    public void testDuplicateFunctionThrowsException() {
        String code = "begin function main()\nend function\nbegin function main()\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for duplicate function");

        assertTrue(ex.getMessage().contains("Duplicate function"), "Error message should mention Duplicate function");
    }

    public void testUndeclaredVariableThrowsException() {
        String code = "begin function main()\nprint(age)\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for undeclared variable");

        assertTrue(ex.getMessage().contains("used before being defined"), "Error message should mention used before defined");
    }

    public void testTypeMismatchThrowsException() {
        String code = "begin function main()\nage = 30\nage = \"Thirty\"\nend function";
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
        String code = "begin function main()\ntotal += 10\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for compound assignment on undeclared variable");

        assertTrue(ex.getMessage().contains("Cannot use compound assignment"), "Error message should mention compound assignment");
    }

    public void testCounterVariableScopedToLoop() {
        String code = "begin function main()\n" +
                      "begin loop(5) as i\n" +
                      "    print(i)\n" +
                      "end loop\n" +
                      "print(i)\n" +
                      "end function";
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
        String code = "begin function main()\n" +
                      "break loop\n" +
                      "end function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for Break Loop outside loop");

        assertTrue(ex.getMessage().contains("'break loop' cannot be used outside of a loop"),
                "Error message should mention break loop cannot be used outside of a loop");
    }

    public void testContinueOutsideLoopThrowsException() {
        String code = "begin function main()\n" +
                      "continue loop\n" +
                      "end function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for Continue Loop outside loop");

        assertTrue(ex.getMessage().contains("'continue loop' cannot be used outside of a loop"),
                "Error message should mention continue loop cannot be used outside of a loop");
    }

    public void testNonNumericLoopCountThrowsException() {
        String code = "begin function main()\n" +
                      "begin loop(\"not a number\")\n" +
                      "    print(\"Hello\")\n" +
                      "end loop\n" +
                      "end function";
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
        String code = "begin function main()\n" +
                      "i = 10\n" +
                      "begin loop(5) as i\n" +
                      "    print(i)\n" +
                      "end loop\n" +
                      "end function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for declaring counter matching existing variable");

        assertTrue(ex.getMessage().contains("already declared in an enclosing scope"),
                "Error message should mention variable already declared in an enclosing scope");
    }

    public void testMixedTypeArrayThrowsException() {
        String code = "begin function main()\na = {1, \"hello\", 3}\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for mixed-type array");

        assertTrue(ex.getMessage().contains("all elements must have the same type"),
                "Error message should mention all elements must have the same type, got: " + ex.getMessage());
    }

    public void testNonIntegerIndexThrowsException() {
        String code = "begin function main()\na = {1, 2, 3}\nprint(a[\"zero\"])\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for non-integer array index");

        assertTrue(ex.getMessage().contains("Array index must be an integer"),
                "Error message should mention array index must be an integer, got: " + ex.getMessage());
    }

    public void testIndexAccessOnNonArrayThrowsException() {
        String code = "begin function main()\nx = 10\nprint(x[0])\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for indexing into non-array");

        assertTrue(ex.getMessage().contains("Cannot index into non-array"),
                "Error message should mention cannot index into non-array, got: " + ex.getMessage());
    }

    public void testUnknownMethodOnArrayThrowsException() {
        String code = "begin function main()\na = {1, 2, 3}\na.unknownMethod(5)\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for unknown array method");

        assertTrue(ex.getMessage().contains("Unknown array method"),
                "Error message should mention unknown array method, got: " + ex.getMessage());
    }

    public void testInvalidMethodArgumentCountThrowsException() {
        String code = "begin function main()\na = {1, 2, 3}\na.addToEnd()\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for invalid method argument count");

        assertTrue(ex.getMessage().contains("Method 'addToEnd' expects 1 argument"),
                "Error message should mention method expects 1 argument, got: " + ex.getMessage());
    }

    public void testNestedArrayTypeMismatchThrowsException() {
        String code = "begin function main()\na = {{1, 2}, {\"a\", \"b\"}}\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for nested array type mismatch");

        assertTrue(ex.getMessage().contains("Nested array element type mismatch"),
                "Error message should mention nested array element type mismatch, got: " + ex.getMessage());
    }

    public void testIndexAssignTypeMismatchThrowsException() {
        String code = "begin function main()\na = {1, 2, 3}\na[0] = \"hello\"\nend function";
        Lexer[] lexerRef = new Lexer[1];
        ProgramNode program = parse(code, lexerRef);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(lexerRef[0]);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            analyzer.analyze(program);
        }, "Should throw SemanticException for assigning wrong type to array element");

        assertTrue(ex.getMessage().contains("Cannot assign value of type STRING to array element of type INT"),
                "Error message should mention Cannot assign value of type STRING to array element of type INT, got: " + ex.getMessage());
    }
}
