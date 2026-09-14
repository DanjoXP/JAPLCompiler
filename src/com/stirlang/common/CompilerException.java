package com.stirlang.common;

/**
 * Base exception class for Stirlang Compiler Errors.
 * Formats user-friendly, educational error messages indicating exact line and column locations.
 */
public class CompilerException extends RuntimeException {
    private final SourceLocation location;
    private String sourceLine;

    public CompilerException(String message, SourceLocation location) {
        super(message);
        this.location = location;
    }

    public CompilerException(String message, SourceLocation location, String sourceLine) {
        super(message);
        this.location = location;
        this.sourceLine = sourceLine;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public String getSourceLine() {
        return sourceLine;
    }

    public void setSourceLine(String sourceLine) {
        this.sourceLine = sourceLine;
    }

    /**
     * Formats the compiler error in a clean, developer-friendly manner:
     *
     * Stirlang Compiler Error: Line 6, Column 5
     * >     End Function
     *       ^
     * Expected 'end if' before 'End Function'.
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stirlang Compiler Error\n");
        if (location != null) {
            sb.append("Line ").append(location.getLine());
            sb.append(", Column ").append(location.getColumn()).append(": ");
        }
        sb.append(getMessage());

        if (sourceLine != null && !sourceLine.isEmpty() && location != null) {
            sb.append("\n\n    ").append(sourceLine).append("\n    ");
            int col = Math.max(1, location.getColumn());
            for (int i = 1; i < col; i++) {
                sb.append(" ");
            }
            sb.append("^");
        }

        return sb.toString();
    }
}
