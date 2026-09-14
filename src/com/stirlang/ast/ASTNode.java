package com.stirlang.ast;

import com.stirlang.common.SourceLocation;

/**
 * Base class for all nodes in the Stirlang Abstract Syntax Tree (AST).
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
