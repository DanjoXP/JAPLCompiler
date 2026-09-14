package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import com.stirlang.semantic.DataType;
import java.util.List;

/**
 * Represents an array literal in Stirlang: {1, 2, 3, 4} or {}.
 */
public class ArrayLiteralNode extends ExpressionNode {
    private final List<ExpressionNode> elements;
    private DataType elementType = DataType.ANY;

    public ArrayLiteralNode(List<ExpressionNode> elements, SourceLocation location) {
        super(location);
        this.elements = elements;
        setEvaluatedType(DataType.ARRAY);
    }

    public List<ExpressionNode> getElements() {
        return elements;
    }

    public DataType getElementType() {
        return elementType;
    }

    public void setElementType(DataType elementType) {
        this.elementType = elementType;
    }
}
