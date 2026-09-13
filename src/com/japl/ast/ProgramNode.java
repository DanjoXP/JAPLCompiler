package com.japl.ast;

import com.japl.common.SourceLocation;
import java.util.Collections;
import java.util.List;

/**
 * Root node of a JAPL program containing top-level function declarations.
 */
public class ProgramNode extends ASTNode {
    private final List<FunctionNode> functions;

    public ProgramNode(List<FunctionNode> functions, SourceLocation location) {
        super(location);
        this.functions = functions != null ? functions : Collections.emptyList();
    }

    public List<FunctionNode> getFunctions() {
        return functions;
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
