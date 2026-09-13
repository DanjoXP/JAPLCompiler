package com.japl.ast;

import com.japl.common.SourceLocation;
import com.japl.semantic.DataType;

/**
 * Base class for all expression nodes in JAPL.
 * Stores inferred data type computed during semantic analysis.
 */
public abstract class ExpressionNode extends ASTNode {
    private DataType evaluatedType = DataType.ANY;

    public ExpressionNode(SourceLocation location) {
        super(location);
    }

    public DataType getEvaluatedType() {
        return evaluatedType;
    }

    public void setEvaluatedType(DataType evaluatedType) {
        this.evaluatedType = evaluatedType;
    }
}
