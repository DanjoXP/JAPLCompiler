package com.stirlang.test;

import com.stirlang.common.LexerException;
import com.stirlang.lexer.Lexer;
import com.stirlang.lexer.Token;
import com.stirlang.lexer.TokenType;

import java.util.List;

import static com.stirlang.test.StirlangTestRunner.*;

public class LexerTest {

    public void testKeywords() {
        String code = "begin function end if else print return loop as break continue";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.BEGIN, tokens.get(0).getType());
        assertEquals(TokenType.FUNCTION, tokens.get(1).getType());
        assertEquals(TokenType.END, tokens.get(2).getType());
        assertEquals(TokenType.IF, tokens.get(3).getType());
        assertEquals(TokenType.ELSE, tokens.get(4).getType());
        assertEquals(TokenType.PRINT, tokens.get(5).getType());
        assertEquals(TokenType.RETURN, tokens.get(6).getType());
        assertEquals(TokenType.LOOP, tokens.get(7).getType());
        assertEquals(TokenType.AS, tokens.get(8).getType());
        assertEquals(TokenType.BREAK, tokens.get(9).getType());
        assertEquals(TokenType.CONTINUE, tokens.get(10).getType());
        assertEquals(TokenType.EOF, tokens.get(11).getType());
    }

    public void testLiterals() {
        String code = "age = 30 name = \"John\" rate = 3.14 active = true inactive = false";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(1).getType());
        assertEquals(TokenType.INTEGER, tokens.get(2).getType());
        assertEquals(30, tokens.get(2).getLiteral());

        assertEquals(TokenType.IDENTIFIER, tokens.get(3).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(4).getType());
        assertEquals(TokenType.STRING, tokens.get(5).getType());
        assertEquals("John", tokens.get(5).getLiteral());

        assertEquals(TokenType.IDENTIFIER, tokens.get(6).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(7).getType());
        assertEquals(TokenType.DECIMAL, tokens.get(8).getType());
        assertEquals(3.14, tokens.get(8).getLiteral());

        assertEquals(TokenType.IDENTIFIER, tokens.get(9).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(10).getType());
        assertEquals(TokenType.BOOLEAN, tokens.get(11).getType());
        assertEquals(Boolean.TRUE, tokens.get(11).getLiteral());

        assertEquals(TokenType.IDENTIFIER, tokens.get(12).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(13).getType());
        assertEquals(TokenType.BOOLEAN, tokens.get(14).getType());
        assertEquals(Boolean.FALSE, tokens.get(14).getLiteral());
    }

    public void testStringEscapes() {
        String code = "\"Hello\\nWorld\\t\\\"Quoted\\\"\"";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.STRING, tokens.get(0).getType());
        assertEquals("Hello\nWorld\t\"Quoted\"", tokens.get(0).getLiteral());
    }

    public void testReadableOperators() {
        String code = "age greaterThan 18 and active equalTo true or score isNot 0 not flag lessThan 5";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.GREATER_THAN_EQUAL, tokens.get(1).getType());
        assertEquals(TokenType.INTEGER, tokens.get(2).getType());
        assertEquals(TokenType.AND, tokens.get(3).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
        assertEquals(TokenType.EQUAL, tokens.get(5).getType());
        assertEquals(TokenType.BOOLEAN, tokens.get(6).getType());
        assertEquals(TokenType.OR, tokens.get(7).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(8).getType());
        assertEquals(TokenType.NOT_EQUAL, tokens.get(9).getType());
        assertEquals(TokenType.INTEGER, tokens.get(10).getType());
        assertEquals(TokenType.NOT, tokens.get(11).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(12).getType());
        assertEquals(TokenType.LESS_THAN_EQUAL, tokens.get(13).getType());
        assertEquals(TokenType.INTEGER, tokens.get(14).getType());
    }

    public void testSymbolicOperators() {
        String code = "== != >= <= > < + - * / % = += -= *= /= %=";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.EQUAL, tokens.get(0).getType());
        assertEquals(TokenType.NOT_EQUAL, tokens.get(1).getType());
        assertEquals(TokenType.GREATER_THAN_EQUAL, tokens.get(2).getType());
        assertEquals(TokenType.LESS_THAN_EQUAL, tokens.get(3).getType());
        assertEquals(TokenType.GREATER_THAN, tokens.get(4).getType());
        assertEquals(TokenType.LESS_THAN, tokens.get(5).getType());
        assertEquals(TokenType.PLUS, tokens.get(6).getType());
        assertEquals(TokenType.MINUS, tokens.get(7).getType());
        assertEquals(TokenType.MULTIPLY, tokens.get(8).getType());
        assertEquals(TokenType.DIVIDE, tokens.get(9).getType());
        assertEquals(TokenType.MODULO, tokens.get(10).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(11).getType());
        assertEquals(TokenType.PLUS_ASSIGN, tokens.get(12).getType());
        assertEquals(TokenType.MINUS_ASSIGN, tokens.get(13).getType());
        assertEquals(TokenType.MULTIPLY_ASSIGN, tokens.get(14).getType());
        assertEquals(TokenType.DIVIDE_ASSIGN, tokens.get(15).getType());
        assertEquals(TokenType.MODULO_ASSIGN, tokens.get(16).getType());
    }

    public void testComments() {
        String code = "x = 10 # This is a hash comment\n// This is a slash comment\ny = 20";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(1).getType());
        assertEquals(TokenType.INTEGER, tokens.get(2).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(3).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(4).getType());
        assertEquals(TokenType.INTEGER, tokens.get(5).getType());
        assertEquals(TokenType.EOF, tokens.get(6).getType());
    }

    public void testUnterminatedStringThrowsException() {
        assertThrows(LexerException.class, () -> {
            new Lexer("\"Unclosed string literal").tokenize();
        }, "Should throw LexerException for unclosed string");
    }

    public void testUnexpectedCharacterThrowsException() {
        assertThrows(LexerException.class, () -> {
            new Lexer("x = @ + 1").tokenize();
        }, "Should throw LexerException for unrecognized symbol");
    }

    public void testArrayTokens() {
        String code = "a = {1, 2, 3} a[0] a.addToEnd(4)";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.ASSIGN, tokens.get(1).getType());
        assertEquals(TokenType.LEFT_BRACE, tokens.get(2).getType());
        assertEquals(TokenType.INTEGER, tokens.get(3).getType());
        assertEquals(TokenType.COMMA, tokens.get(4).getType());
        assertEquals(TokenType.INTEGER, tokens.get(5).getType());
        assertEquals(TokenType.COMMA, tokens.get(6).getType());
        assertEquals(TokenType.INTEGER, tokens.get(7).getType());
        assertEquals(TokenType.RIGHT_BRACE, tokens.get(8).getType());

        assertEquals(TokenType.IDENTIFIER, tokens.get(9).getType());
        assertEquals(TokenType.LEFT_BRACKET, tokens.get(10).getType());
        assertEquals(TokenType.INTEGER, tokens.get(11).getType());
        assertEquals(TokenType.RIGHT_BRACKET, tokens.get(12).getType());

        assertEquals(TokenType.IDENTIFIER, tokens.get(13).getType());
        assertEquals(TokenType.DOT, tokens.get(14).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(15).getType());
        assertEquals("addToEnd", tokens.get(15).getLexeme());
        assertEquals(TokenType.LEFT_PAREN, tokens.get(16).getType());
        assertEquals(TokenType.INTEGER, tokens.get(17).getType());
        assertEquals(TokenType.RIGHT_PAREN, tokens.get(18).getType());
    }
}
