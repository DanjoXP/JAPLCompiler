package com.japl.lexer;

/**
 * Enumeration of all token types in the JAPL programming language.
 */
public enum TokenType {
    // Keywords
    BEGIN("Begin"),
    FUNCTION("Function"),
    END("End"),
    IF("if"),
    ELSE("else"),
    PRINT("print"),
    RETURN("return"),

    // Literals & Identifiers
    IDENTIFIER("identifier"),
    INTEGER("integer"),
    DECIMAL("decimal"),
    STRING("string"),
    BOOLEAN("boolean"),

    // Delimiters & Punctuation
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    COMMA(","),

    // Arithmetic Operators
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),

    // Assignment Operators
    ASSIGN("="),
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MODULO_ASSIGN("%="),

    // Comparison Operators
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<="),

    // Logical Operators
    AND("&&"),
    OR("||"),
    NOT("!"),

    // End of file
    EOF("EOF");

    private final String representation;

    TokenType(String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
