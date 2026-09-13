package com.japl.ast;

import com.japl.common.SourceLocation;

/**
 * Base class for all nodes in the JAPL Abstract Syntax Tree (AST).
 */
public abstract class ASTNode {
    private final SourceLocation location;

    public ASTNode(SourceLocation location) {
        this.location = location;
    }

    public SourceLocation getLocation() {
        return location;
    }
}
