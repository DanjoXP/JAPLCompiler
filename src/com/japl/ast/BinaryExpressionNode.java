package com.japl.ast;

import com.japl.common.SourceLocation;
import com.japl.lexer.TokenType;

/**
 * Represents a binary operation between two expressions.
 * Examples: a + b, x equalTo y, age < 18 and active == true
 */
public class BinaryExpressionNode extends ExpressionNode {
    private final ExpressionNode left;
    private final TokenType operator;
    private final ExpressionNode right;

    public BinaryExpressionNode(ExpressionNode left, TokenType operator, ExpressionNode right, SourceLocation location) {
        super(location);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getRight() {
        return right;
    }
}
