package com.stirlang.ast;

import com.stirlang.common.SourceLocation;

/**
 * Represents an identifier referring to a variable.
 * Example: age
 */
public class VariableExpressionNode extends ExpressionNode {
    private final String name;

    public VariableExpressionNode(String name, SourceLocation location) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
