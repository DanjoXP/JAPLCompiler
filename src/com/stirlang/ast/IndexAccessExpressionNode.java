package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import com.stirlang.semantic.DataType;

/**
 * Represents indexing into an array: a[0], a[-1], or chained a[0][1].
 */
public class IndexAccessExpressionNode extends ExpressionNode {
    private final ExpressionNode target;
    private final ExpressionNode index;
    private DataType elementType = DataType.ANY;

    public IndexAccessExpressionNode(ExpressionNode target, ExpressionNode index, SourceLocation location) {
        super(location);
        this.target = target;
        this.index = index;
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public ExpressionNode getIndex() {
        return index;
    }

    public DataType getElementType() {
        return elementType;
    }

    public void setElementType(DataType elementType) {
        this.elementType = elementType;
    }
}
