package com.stirlang.test;

import com.stirlang.ast.*;
import com.stirlang.common.ParserException;
import com.stirlang.lexer.Lexer;
import com.stirlang.lexer.Token;
import com.stirlang.lexer.TokenType;
import com.stirlang.parser.Parser;

import java.util.List;

import static com.stirlang.test.StirlangTestRunner.*;

public class ParserTest {

    public void testFunctionDeclaration() {
        String code = "Begin Function greet(name)\nprint(name)\nEnd Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        assertEquals(1, program.getFunctions().size());
        FunctionNode fn = program.getFunctions().get(0);
        assertEquals("greet", fn.getName());
        assertEquals(1, fn.getParameters().size());
        assertEquals("name", fn.getParameters().get(0));
        assertEquals(1, fn.getBody().getStatements().size());
        assertTrue(fn.getBody().getStatements().get(0) instanceof PrintStatementNode);
    }

    public void testOperatorPrecedence() {
        String code = "Begin Function main()\nresult = 10 + 5 * 2\nEnd Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        AssignmentStatementNode assign = (AssignmentStatementNode) fn.getBody().getStatements().get(0);
        assertEquals("result", assign.getVariableName());

        // result = 10 + (5 * 2)
        assertTrue(assign.getValue() instanceof BinaryExpressionNode, "Value should be binary expression");
        BinaryExpressionNode addExpr = (BinaryExpressionNode) assign.getValue();
        assertEquals(TokenType.PLUS, addExpr.getOperator());
        assertTrue(addExpr.getLeft() instanceof LiteralExpressionNode);
        assertTrue(addExpr.getRight() instanceof BinaryExpressionNode);

        BinaryExpressionNode mulExpr = (BinaryExpressionNode) addExpr.getRight();
        assertEquals(TokenType.MULTIPLY, mulExpr.getOperator());
    }

    public void testIfElseParsing() {
        String code = "Begin Function main()\n" +
                      "if age < 18\n" +
                      "    print(\"Underage\")\n" +
                      "else\n" +
                      "    print(\"Adult\")\n" +
                      "end if\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        assertEquals(1, fn.getBody().getStatements().size());
        assertTrue(fn.getBody().getStatements().get(0) instanceof IfStatementNode);

        IfStatementNode ifStmt = (IfStatementNode) fn.getBody().getStatements().get(0);
        assertEquals(1, ifStmt.getThenBranch().getStatements().size());
        assertNotNull(ifStmt.getElseBranch(), "Else branch should be present");
        assertEquals(1, ifStmt.getElseBranch().getStatements().size());
    }

    public void testMissingEndFunctionThrowsException() {
        String code = "Begin Function main()\nage = 30\n";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing End Function");

        assertTrue(ex.getMessage().contains("Missing 'End Function'"), "Error message should mention Missing 'End Function'");
    }

    public void testMissingEndIfBeforeEndFunction() {
        String code = "Begin Function main()\n" +
                      "if age < 18\n" +
                      "    print(\"Hello\")\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing end if");

        assertTrue(ex.getMessage().contains("Expected 'end if' before 'End Function'"),
                "Message should contain: Expected 'end if' before 'End Function'");
    }

    public void testInfiniteLoopParsing() {
        String code = "Begin Function main()\n" +
                      "Start Loop\n" +
                      "    print(\"Forever\")\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        assertEquals(1, fn.getBody().getStatements().size());
        assertTrue(fn.getBody().getStatements().get(0) instanceof LoopStatementNode);

        LoopStatementNode loop = (LoopStatementNode) fn.getBody().getStatements().get(0);
        assertTrue(loop.isInfinite());
        assertTrue(!loop.isCounted());
        assertTrue(!loop.hasExposedCounter());
        assertEquals(1, loop.getBody().getStatements().size());
    }

    public void testCountedLoopWithExposedCounter() {
        String code = "Begin Function main()\n" +
                      "Start Loop(10) as i\n" +
                      "    print(i)\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        LoopStatementNode loop = (LoopStatementNode) fn.getBody().getStatements().get(0);
        assertTrue(!loop.isInfinite());
        assertTrue(loop.isCounted());
        assertTrue(loop.hasExposedCounter());
        assertEquals("i", loop.getCounterVariable());
    }

    public void testCountedLoopWithoutExposedCounter() {
        String code = "Begin Function main()\n" +
                      "Start Loop(5)\n" +
                      "    print(\"Hi\")\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        LoopStatementNode loop = (LoopStatementNode) fn.getBody().getStatements().get(0);
        assertTrue(!loop.isInfinite());
        assertTrue(loop.isCounted());
        assertTrue(!loop.hasExposedCounter());
    }

    public void testBreakAndContinueInLoop() {
        String code = "Begin Function main()\n" +
                      "Start Loop\n" +
                      "    Continue Loop\n" +
                      "    Break Loop\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        LoopStatementNode loop = (LoopStatementNode) fn.getBody().getStatements().get(0);
        assertEquals(2, loop.getBody().getStatements().size());
        assertTrue(loop.getBody().getStatements().get(0) instanceof ContinueStatementNode);
        assertTrue(loop.getBody().getStatements().get(1) instanceof BreakStatementNode);
    }

    public void testMisplacedEndLoopThrowsException() {
        String code = "Begin Function main()\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for misplaced End Loop");

        assertTrue(ex.getMessage().contains("Unexpected 'End Loop' without matching 'Start Loop'"),
                "Message should complain about unexpected End Loop");
    }

    public void testMissingEndLoopBeforeFunctionEndThrowsException() {
        String code = "Begin Function main()\n" +
                      "Start Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing End Loop before End Function");

        assertTrue(ex.getMessage().contains("Expected 'End Loop' before 'End Function'"),
                "Message should contain: Expected 'End Loop' before 'End Function'");
    }

    public void testMissingEndIfBeforeEndLoopThrowsException() {
        String code = "Begin Function main()\n" +
                      "Start Loop\n" +
                      "if true\n" +
                      "    print(\"Hi\")\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing end if before End Loop");

        assertTrue(ex.getMessage().contains("Expected 'end if' before 'End Loop'"),
                "Message should contain: Expected 'end if' before 'End Loop'");
    }

    public void testInfiniteLoopWithAsThrowsException() {
        String code = "Begin Function main()\n" +
                      "Start Loop as i\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for Start Loop as i");

        assertTrue(ex.getMessage().contains("An infinite loop cannot declare a counter variable with 'as'"),
                "Message should mention infinite loop cannot declare counter variable with as");
    }

    public void testBreakWithoutLoopThrowsException() {
        String code = "Begin Function main()\n" +
                      "Start Loop\n" +
                      "    Break\n" +
                      "End Loop\n" +
                      "End Function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for Break without Loop");

        assertTrue(ex.getMessage().contains("Expected 'Loop' after 'Break'"),
                "Message should contain: Expected 'Loop' after 'Break'");
    }
}
