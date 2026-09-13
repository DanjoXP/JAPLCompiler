package com.japl.ast;

import com.japl.common.SourceLocation;
import com.japl.lexer.TokenType;
import com.japl.semantic.DataType;

/**
 * Represents a variable assignment or compound assignment statement.
 * Example: age = 30 or total += 10
 */
public class AssignmentStatementNode extends StatementNode {
    private final String variableName;
    private final TokenType operator;
    private final ExpressionNode value;
    private boolean declaration = false;
    private DataType variableType = DataType.ANY;

    public AssignmentStatementNode(String variableName, TokenType operator, ExpressionNode value, SourceLocation location) {
        super(location);
        this.variableName = variableName;
        this.operator = operator;
        this.value = value;
    }

    public String getVariableName() {
        return variableName;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getValue() {
        return value;
    }

    public boolean isDeclaration() {
        return declaration;
    }

    public void setDeclaration(boolean declaration) {
        this.declaration = declaration;
    }

    public DataType getVariableType() {
        return variableType;
    }

    public void setVariableType(DataType variableType) {
        this.variableType = variableType;
    }
}
