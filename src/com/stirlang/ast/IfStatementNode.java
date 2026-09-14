package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an if / else if / else conditional statement.
 *
 * Example:
 * if age < 18
 *     print("Underage")
 * else
 *     print("Adult")
 * end if
 */
public class IfStatementNode extends StatementNode {
    private final ExpressionNode condition;
    private final BlockNode thenBranch;
    private final List<ElseIfBranch> elseIfBranches;
    private final BlockNode elseBranch;

    public static class ElseIfBranch {
        private final ExpressionNode condition;
        private final BlockNode body;
        private final SourceLocation location;

        public ElseIfBranch(ExpressionNode condition, BlockNode body, SourceLocation location) {
            this.condition = condition;
            this.body = body;
            this.location = location;
        }

        public ExpressionNode getCondition() {
            return condition;
        }

        public BlockNode getBody() {
            return body;
        }

        public SourceLocation getLocation() {
            return location;
        }
    }

    public IfStatementNode(ExpressionNode condition,
                           BlockNode thenBranch,
                           List<ElseIfBranch> elseIfBranches,
                           BlockNode elseBranch,
                           SourceLocation location) {
        super(location);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseIfBranches = elseIfBranches != null ? elseIfBranches : new ArrayList<>();
        this.elseBranch = elseBranch;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public BlockNode getThenBranch() {
        return thenBranch;
    }

    public List<ElseIfBranch> getElseIfBranches() {
        return elseIfBranches;
    }

    public BlockNode getElseBranch() {
        return elseBranch;
    }
}
