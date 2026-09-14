package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import java.util.List;

/**
 * Represents a method call on an expression:
 * Example: a.addToEnd(20), a.add(23, 4), a.remove(4), a.removeIndex(-1).
 */
public class MethodCallExpressionNode extends ExpressionNode {
    private final ExpressionNode target;
    private final String methodName;
    private final List<ExpressionNode> arguments;

    public MethodCallExpressionNode(ExpressionNode target, String methodName, List<ExpressionNode> arguments, SourceLocation location) {
        super(location);
        this.target = target;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }
}
