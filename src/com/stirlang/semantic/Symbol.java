package com.stirlang.semantic;

import com.stirlang.common.SourceLocation;

/**
 * Represents a declared symbol (variable or parameter) in the symbol table.
 */
public class Symbol {
    private final String name;
    private DataType type;
    private final SourceLocation location;
    private final boolean isParameter;

    private DataType arrayElementType = null;
    private DataType nestedArrayElementType = null;

    public Symbol(String name, DataType type, SourceLocation location, boolean isParameter) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.isParameter = isParameter;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public DataType getArrayElementType() {
        return arrayElementType;
    }

    public void setArrayElementType(DataType arrayElementType) {
        this.arrayElementType = arrayElementType;
    }

    public DataType getNestedArrayElementType() {
        return nestedArrayElementType;
    }

    public void setNestedArrayElementType(DataType nestedArrayElementType) {
        this.nestedArrayElementType = nestedArrayElementType;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public boolean isParameter() {
        return isParameter;
    }
}
