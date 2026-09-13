package com.japl.ast;

import com.japl.common.SourceLocation;

/**
 * Represents a return statement.
 * Example: return result
 */
public class ReturnStatementNode extends StatementNode {
    private final ExpressionNode value;

    public ReturnStatementNode(ExpressionNode value, SourceLocation location) {
        super(location);
        this.value = value;
    }

    public ExpressionNode getValue() {
        return value;
    }
}
