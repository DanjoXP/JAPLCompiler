package com.stirlang.semantic;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages scoped symbols (variables and parameters) within functions and blocks.
 */
public class SymbolTable {
    private final SymbolTable parent;
    private final Map<String, Symbol> symbols = new HashMap<>();

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public SymbolTable() {
        this(null);
    }

    public SymbolTable getParent() {
        return parent;
    }

    /**
     * Declares a new symbol in the current scope.
     */
    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return false;
        }
        symbols.put(symbol.getName(), symbol);
        return true;
    }

    /**
     * Resolves a symbol by looking up the scope chain.
     */
    public Symbol resolve(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return null;
    }

    /**
     * Checks if a symbol is declared directly in this current scope.
     */
    public boolean containsCurrentScope(String name) {
        return symbols.containsKey(name);
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }
}
