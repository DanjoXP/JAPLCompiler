package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import java.util.Collections;
import java.util.List;

/**
 * Represents a block (sequence) of statements.
 */
public class BlockNode extends StatementNode {
    private final List<StatementNode> statements;

    public BlockNode(List<StatementNode> statements, SourceLocation location) {
        super(location);
        this.statements = statements != null ? statements : Collections.emptyList();
    }

    public List<StatementNode> getStatements() {
        return statements;
    }
}
