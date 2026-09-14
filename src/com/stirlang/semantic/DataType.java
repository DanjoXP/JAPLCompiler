package com.stirlang.semantic;

/**
 * Data types supported by Stirlang.
 */
public enum DataType {
    INT("int"),
    DECIMAL("double"),
    STRING("String"),
    BOOLEAN("boolean"),
    ARRAY("StirlangArray"),
    VOID("void"),
    ANY("Object");

    private final String javaTypeName;

    DataType(String javaTypeName) {
        this.javaTypeName = javaTypeName;
    }

    public String getJavaTypeName() {
        return javaTypeName;
    }

    public boolean isNumeric() {
        return this == INT || this == DECIMAL;
    }

    public boolean isArray() {
        return this == ARRAY;
    }
}
