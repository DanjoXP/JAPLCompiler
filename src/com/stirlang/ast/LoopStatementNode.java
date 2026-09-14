package com.stirlang.ast;

import com.stirlang.common.SourceLocation;

/**
 * Represents a loop statement in Stirlang.
 *
 * Supports three forms:
 * 1. Infinite loop:
 *    begin loop
 *        // code
 *    end loop
 *
 * 2. Counted loop with exposed counter:
 *    begin loop(10) as i
 *        print(i)
 *    end loop
 *
 * 3. Counted loop without exposed counter:
 *    begin loop(10)
 *        // code
 *    end loop
 */
public class LoopStatementNode extends StatementNode {
    private final ExpressionNode countExpression;
    private final String counterVariable;
    private final BlockNode body;

    public LoopStatementNode(ExpressionNode countExpression, String counterVariable, BlockNode body, SourceLocation location) {
        super(location);
        this.countExpression = countExpression;
        this.counterVariable = counterVariable;
        this.body = body;
    }

    public boolean isInfinite() {
        return countExpression == null;
    }

    public boolean isCounted() {
        return countExpression != null;
    }

    public boolean hasExposedCounter() {
        return counterVariable != null && !counterVariable.isEmpty();
    }

    public ExpressionNode getCountExpression() {
        return countExpression;
    }

    public String getCounterVariable() {
        return counterVariable;
    }

    public BlockNode getBody() {
        return body;
    }
}
