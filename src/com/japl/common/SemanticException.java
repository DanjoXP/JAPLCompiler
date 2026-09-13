package com.japl.common;

/**
 * Exception thrown during semantic validation (type checking, scope resolution, etc.).
 */
public class SemanticException extends CompilerException {
    public SemanticException(String message, SourceLocation location) {
        super(message, location);
    }

    public SemanticException(String message, SourceLocation location, String sourceLine) {
        super(message, location, sourceLine);
    }
}
