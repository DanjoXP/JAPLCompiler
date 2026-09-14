package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import java.util.Collections;
import java.util.List;

/**
 * Root node of a Stirlang program containing top-level function declarations.
 */
public class ProgramNode extends ASTNode {
    private final List<FunctionNode> functions;
    private final List<StatementNode> topLevelStatements;

    public ProgramNode(List<FunctionNode> functions, List<StatementNode> topLevelStatements, SourceLocation location) {
        super(location);
        this.functions = functions != null ? functions : Collections.emptyList();
        this.topLevelStatements = topLevelStatements != null ? topLevelStatements : Collections.emptyList();
    }

    public ProgramNode(List<FunctionNode> functions, SourceLocation location) {
        this(functions, Collections.emptyList(), location);
    }

    public List<FunctionNode> getFunctions() {
        return functions;
    }

    public List<StatementNode> getTopLevelStatements() {
        return topLevelStatements;
    }

    public boolean hasTopLevelStatements() {
        return !topLevelStatements.isEmpty();
    }

    public FunctionNode getMainFunction() {
        for (FunctionNode fn : functions) {
            if (fn.isMain()) {
                return fn;
            }
        }
        return null;
    }
}
