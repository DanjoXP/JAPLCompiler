package com.japl.ast;

import com.japl.common.SourceLocation;
import com.japl.lexer.TokenType;

/**
 * Represents a unary expression.
 * Examples: -count, not active, !valid
 */
public class UnaryExpressionNode extends ExpressionNode {
    private final TokenType operator;
    private final ExpressionNode operand;

    public UnaryExpressionNode(TokenType operator, ExpressionNode operand, SourceLocation location) {
        super(location);
        this.operator = operator;
        this.operand = operand;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }
}
