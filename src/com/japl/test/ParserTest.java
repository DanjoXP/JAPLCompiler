package com.japl.test;

import com.japl.ast.*;
import com.japl.common.ParserException;
import com.japl.lexer.Lexer;
import com.japl.lexer.Token;
import com.japl.lexer.TokenType;
import com.japl.parser.Parser;

import java.util.List;

import static com.japl.test.JAPLTestRunner.*;

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
}
