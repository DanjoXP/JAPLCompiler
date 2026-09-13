package com.japl.ast;

import com.japl.common.SourceLocation;
import java.util.Collections;
import java.util.List;

/**
 * Represents a function call expression.
 * Example: greet("Alice") or add(x, y)
 */
public class FunctionCallExpressionNode extends ExpressionNode {
    private final String functionName;
    private final List<ExpressionNode> arguments;

    public FunctionCallExpressionNode(String functionName, List<ExpressionNode> arguments, SourceLocation location) {
        super(location);
        this.functionName = functionName;
        this.arguments = arguments != null ? arguments : Collections.emptyList();
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }
}
