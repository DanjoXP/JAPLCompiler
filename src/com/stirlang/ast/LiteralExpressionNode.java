package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import com.stirlang.semantic.DataType;

/**
 * Represents a literal value (Integer, Decimal, String, or Boolean).
 */
public class LiteralExpressionNode extends ExpressionNode {
    private final Object value;

    public LiteralExpressionNode(DataType type, Object value, SourceLocation location) {
        super(location);
        this.value = value;
        setEvaluatedType(type);
    }

    public Object getValue() {
        return value;
    }
}
