package com.japl.ast;

import com.japl.common.SourceLocation;

/**
 * Base class for all statement nodes in JAPL.
 */
public abstract class StatementNode extends ASTNode {
    public StatementNode(SourceLocation location) {
        super(location);
    }
}
