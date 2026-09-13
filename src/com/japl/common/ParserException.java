package com.japl.common;

/**
 * Exception thrown during parsing (syntactic analysis).
 */
public class ParserException extends CompilerException {
    public ParserException(String message, SourceLocation location) {
        super(message, location);
    }

    public ParserException(String message, SourceLocation location, String sourceLine) {
        super(message, location, sourceLine);
    }
}
