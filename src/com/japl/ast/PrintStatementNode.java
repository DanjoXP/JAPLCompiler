package com.japl.ast;

import com.japl.common.SourceLocation;

/**
 * Represents a print statement in JAPL.
 * Example: print("Hello World")
 */
public class PrintStatementNode extends StatementNode {
    private final ExpressionNode expression;

    public PrintStatementNode(ExpressionNode expression, SourceLocation location) {
        super(location);
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }
}
