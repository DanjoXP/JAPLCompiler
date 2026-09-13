package com.japl.common;

/**
 * Represents a specific line and column location in a JAPL source file.
 */
public class SourceLocation {
    private final int line;
    private final int column;

    public SourceLocation(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "Line " + line + ", Column " + column;
    }
}
