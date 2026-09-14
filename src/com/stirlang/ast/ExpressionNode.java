package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import com.stirlang.semantic.DataType;

/**
 * Base class for all expression nodes in Stirlang.
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
