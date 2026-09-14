package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import com.stirlang.lexer.TokenType;

/**
 * Represents an assignment to an indexed array element:
 * Example: a[0] = 10, a[-1] = 20, or a[0][1] = 50.
 */
public class IndexAssignmentStatementNode extends StatementNode {
    private final IndexAccessExpressionNode target;
    private final TokenType operator;
    private final ExpressionNode value;

    public IndexAssignmentStatementNode(IndexAccessExpressionNode target, TokenType operator, ExpressionNode value, SourceLocation location) {
        super(location);
        this.target = target;
        this.operator = operator;
        this.value = value;
    }

    public IndexAccessExpressionNode getTarget() {
        return target;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getValue() {
        return value;
    }
}
