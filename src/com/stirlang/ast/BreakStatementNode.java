package com.stirlang.ast;

import com.stirlang.common.SourceLocation;

/**
 * Represents a 'Break Loop' statement which immediately exits the nearest enclosing loop.
 */
public class BreakStatementNode extends StatementNode {
    public BreakStatementNode(SourceLocation location) {
        super(location);
    }
}
