package com.stirlang.ast;

import com.stirlang.common.SourceLocation;
import com.stirlang.semantic.DataType;
import java.util.Collections;
import java.util.List;

/**
 * Represents a function declaration in Stirlang.
 *
 * Example:
 * Begin Function greet(name)
 *     print(name)
 * End Function
 */
public class FunctionNode extends ASTNode {
    private final String name;
    private final List<String> parameters;
    private final java.util.Map<String, DataType> parameterTypes = new java.util.HashMap<>();
    private final BlockNode body;
    private DataType returnType = DataType.VOID;

    public FunctionNode(String name, List<String> parameters, BlockNode body, SourceLocation location) {
        super(location);
        this.name = name;
        this.parameters = parameters != null ? parameters : Collections.emptyList();
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public BlockNode getBody() {
        return body;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public java.util.Map<String, DataType> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterType(String param, DataType type) {
        parameterTypes.put(param, type);
    }

    public void setReturnType(DataType returnType) {
        this.returnType = returnType;
    }

    public boolean isMain() {
        return "main".equalsIgnoreCase(name);
    }
}
