package com.stirlang.common;

/**
 * Exception thrown during the tokenization (lexing) stage.
 */
public class LexerException extends CompilerException {
    public LexerException(String message, SourceLocation location) {
        super(message, location);
    }

    public LexerException(String message, SourceLocation location, String sourceLine) {
        super(message, location, sourceLine);
    }
}
