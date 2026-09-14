package com.stirlang.runtime;

/**
 * Runtime error thrown by Stirlang runtime operations (such as array out-of-range or type mismatch).
 */
public class StirlangRuntimeError extends RuntimeException {
    public StirlangRuntimeError(String message) {
        super(message);
    }
}
