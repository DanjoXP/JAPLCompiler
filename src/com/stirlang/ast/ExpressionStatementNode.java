package com.stirlang.ast;

import com.stirlang.common.SourceLocation;

/**
 * Represents an expression evaluated as a statement (such as a function call).
 * Example: greet("Alice")
 */
public class ExpressionStatementNode extends StatementNode {
    private final ExpressionNode expression;

    public ExpressionStatementNode(ExpressionNode expression, SourceLocation location) {
        super(location);
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }
}
