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
        String code = "begin function greet(name)\nprint(name)\nend function";
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
        String code = "begin function main()\nresult = 10 + 5 * 2\nend function";
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
        String code = "begin function main()\n" +
                      "if age < 18\n" +
                      "    print(\"Underage\")\n" +
                      "else\n" +
                      "    print(\"Adult\")\n" +
                      "end if\n" +
                      "end function";
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
        String code = "begin function main()\nage = 30\n";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing end function");

        assertTrue(ex.getMessage().contains("Missing 'end function'"), "Error message should mention Missing 'end function'");
    }

    public void testMissingEndIfBeforeEndFunction() {
        String code = "begin function main()\n" +
                      "if age < 18\n" +
                      "    print(\"Hello\")\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing end if");

        assertTrue(ex.getMessage().contains("Expected 'end if' before 'end function'"),
                "Message should contain: Expected 'end if' before 'end function'");
    }

    public void testInfiniteLoopParsing() {
        String code = "begin function main()\n" +
                      "begin loop\n" +
                      "    print(\"Forever\")\n" +
                      "end loop\n" +
                      "end function";
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
        String code = "begin function main()\n" +
                      "begin loop(10) as i\n" +
                      "    print(i)\n" +
                      "end loop\n" +
                      "end function";
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
        String code = "begin function main()\n" +
                      "begin loop(5)\n" +
                      "    print(\"Hi\")\n" +
                      "end loop\n" +
                      "end function";
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
        String code = "begin function main()\n" +
                      "begin loop\n" +
                      "    continue loop\n" +
                      "    break loop\n" +
                      "end loop\n" +
                      "end function";
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
        String code = "begin function main()\n" +
                      "end loop\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for misplaced end loop");

        assertTrue(ex.getMessage().contains("Unexpected 'end loop' without matching 'begin loop'"),
                "Message should complain about unexpected end loop");
    }

    public void testMissingEndLoopBeforeFunctionEndThrowsException() {
        String code = "begin function main()\n" +
                      "begin loop\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing end loop before end function");

        assertTrue(ex.getMessage().contains("Expected 'end loop' before 'end function'"),
                "Message should contain: Expected 'end loop' before 'end function'");
    }

    public void testMissingEndIfBeforeEndLoopThrowsException() {
        String code = "begin function main()\n" +
                      "begin loop\n" +
                      "if true\n" +
                      "    print(\"Hi\")\n" +
                      "end loop\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for missing end if before end loop");

        assertTrue(ex.getMessage().contains("Expected 'end if' before 'end loop'"),
                "Message should contain: Expected 'end if' before 'end loop'");
    }

    public void testInfiniteLoopWithAsThrowsException() {
        String code = "begin function main()\n" +
                      "begin loop as i\n" +
                      "end loop\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for begin loop as i");

        assertTrue(ex.getMessage().contains("An infinite loop cannot declare a counter variable with 'as'"),
                "Message should mention infinite loop cannot declare counter variable with as");
    }

    public void testBreakWithoutLoopThrowsException() {
        String code = "begin function main()\n" +
                      "begin loop\n" +
                      "    break\n" +
                      "end loop\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);

        ParserException ex = assertThrows(ParserException.class, () -> {
            parser.parse();
        }, "Should throw ParserException for break without loop");

        assertTrue(ex.getMessage().contains("Expected 'loop' after 'break'"),
                "Message should contain: Expected 'loop' after 'break'");
    }

    public void testArrayLiteralParsing() {
        String code = "begin function main()\n" +
                      "a = {1, 2, 3}\n" +
                      "b = {}\n" +
                      "c = {{1, 2}, {3, 4}}\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        assertEquals(3, fn.getBody().getStatements().size());

        AssignmentStatementNode stmt1 = (AssignmentStatementNode) fn.getBody().getStatements().get(0);
        assertTrue(stmt1.getValue() instanceof ArrayLiteralNode);
        ArrayLiteralNode arr1 = (ArrayLiteralNode) stmt1.getValue();
        assertEquals(3, arr1.getElements().size());

        AssignmentStatementNode stmt2 = (AssignmentStatementNode) fn.getBody().getStatements().get(1);
        assertTrue(stmt2.getValue() instanceof ArrayLiteralNode);
        ArrayLiteralNode arr2 = (ArrayLiteralNode) stmt2.getValue();
        assertEquals(0, arr2.getElements().size());

        AssignmentStatementNode stmt3 = (AssignmentStatementNode) fn.getBody().getStatements().get(2);
        assertTrue(stmt3.getValue() instanceof ArrayLiteralNode);
        ArrayLiteralNode arr3 = (ArrayLiteralNode) stmt3.getValue();
        assertEquals(2, arr3.getElements().size());
        assertTrue(arr3.getElements().get(0) instanceof ArrayLiteralNode);
    }

    public void testIndexAccessAndAssignmentParsing() {
        String code = "begin function main()\n" +
                      "a = {1, 2}\n" +
                      "print(a[0])\n" +
                      "a[0] = 10\n" +
                      "a[0][1] = 20\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        assertEquals(4, fn.getBody().getStatements().size());

        PrintStatementNode printStmt = (PrintStatementNode) fn.getBody().getStatements().get(1);
        assertTrue(printStmt.getExpression() instanceof IndexAccessExpressionNode);

        StatementNode assign1 = fn.getBody().getStatements().get(2);
        assertTrue(assign1 instanceof IndexAssignmentStatementNode);
        IndexAssignmentStatementNode idxAssign1 = (IndexAssignmentStatementNode) assign1;
        assertTrue(idxAssign1.getTarget().getTarget() instanceof VariableExpressionNode);

        StatementNode assign2 = fn.getBody().getStatements().get(3);
        assertTrue(assign2 instanceof IndexAssignmentStatementNode);
        IndexAssignmentStatementNode idxAssign2 = (IndexAssignmentStatementNode) assign2;
        assertTrue(idxAssign2.getTarget().getTarget() instanceof IndexAccessExpressionNode);
    }

    public void testMethodCallParsing() {
        String code = "begin function main()\n" +
                      "a = {1, 2}\n" +
                      "a.addToEnd(3)\n" +
                      "a.add(10, 0)\n" +
                      "end function";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, lexer);
        ProgramNode program = parser.parse();

        FunctionNode fn = program.getFunctions().get(0);
        assertEquals(3, fn.getBody().getStatements().size());

        StatementNode stmt1 = fn.getBody().getStatements().get(1);
        assertTrue(stmt1 instanceof ExpressionStatementNode);
        ExpressionStatementNode exprStmt1 = (ExpressionStatementNode) stmt1;
        assertTrue(exprStmt1.getExpression() instanceof MethodCallExpressionNode);
        MethodCallExpressionNode call1 = (MethodCallExpressionNode) exprStmt1.getExpression();
        assertEquals("addToEnd", call1.getMethodName());
        assertEquals(1, call1.getArguments().size());

        StatementNode stmt2 = fn.getBody().getStatements().get(2);
        assertTrue(stmt2 instanceof ExpressionStatementNode);
        ExpressionStatementNode exprStmt2 = (ExpressionStatementNode) stmt2;
        assertTrue(exprStmt2.getExpression() instanceof MethodCallExpressionNode);
        MethodCallExpressionNode call2 = (MethodCallExpressionNode) exprStmt2.getExpression();
        assertEquals("add", call2.getMethodName());
        assertEquals(2, call2.getArguments().size());
    }
}
