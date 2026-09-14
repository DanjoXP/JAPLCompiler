package com.stirlang.ast;

import com.stirlang.common.SourceLocation;

/**
 * Represents a 'continue loop' statement which skips to the next iteration of the nearest enclosing loop.
 */
public class ContinueStatementNode extends StatementNode {
    public ContinueStatementNode(SourceLocation location) {
        super(location);
    }
}
