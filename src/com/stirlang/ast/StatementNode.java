package com.stirlang.ast;

import com.stirlang.common.SourceLocation;

/**
 * Base class for all statement nodes in Stirlang.
 */
public abstract class StatementNode extends ASTNode {
    public StatementNode(SourceLocation location) {
        super(location);
    }
}
