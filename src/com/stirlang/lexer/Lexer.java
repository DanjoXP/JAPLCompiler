package com.stirlang.lexer;

import com.stirlang.common.LexerException;
import com.stirlang.common.SourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tokenizes Stirlang source code into a list of Tokens.
 * Supports comments, escape characters in strings, integer & decimal numbers,
 * symbolic operators, and English-readable keywords.
 */
public class Lexer {
    private final String source;
    private final String[] sourceLines;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int lineStartOffset = 0;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        // Core block keywords
        KEYWORDS.put("BEGIN", TokenType.BEGIN);
        KEYWORDS.put("FUNCTION", TokenType.FUNCTION);
        KEYWORDS.put("END", TokenType.END);
        KEYWORDS.put("IF", TokenType.IF);
        KEYWORDS.put("ELSE", TokenType.ELSE);
        KEYWORDS.put("PRINT", TokenType.PRINT);
        KEYWORDS.put("RETURN", TokenType.RETURN);
        KEYWORDS.put("START", TokenType.START);
        KEYWORDS.put("LOOP", TokenType.LOOP);
        KEYWORDS.put("AS", TokenType.AS);
        KEYWORDS.put("BREAK", TokenType.BREAK);
        KEYWORDS.put("CONTINUE", TokenType.CONTINUE);

        // Logical keywords
        KEYWORDS.put("AND", TokenType.AND);
        KEYWORDS.put("OR", TokenType.OR);
        KEYWORDS.put("NOT", TokenType.NOT);

        // Comparison keywords (case-insensitive)
        KEYWORDS.put("EQUALTO", TokenType.EQUAL);
        KEYWORDS.put("ISNOT", TokenType.NOT_EQUAL);
        KEYWORDS.put("GREATERTHAN", TokenType.GREATER_THAN_EQUAL);
        KEYWORDS.put("GREATERTHANOREQUALTO", TokenType.GREATER_THAN_EQUAL);
        KEYWORDS.put("LESSTHAN", TokenType.LESS_THAN_EQUAL);
        KEYWORDS.put("LESSTHANOREQUALTO", TokenType.LESS_THAN_EQUAL);
    }

    public Lexer(String source) {
        if (source != null && source.startsWith("\uFEFF")) {
            source = source.substring(1);
        }
        this.source = source != null ? source : "";
        this.sourceLines = this.source.split("\\r?\\n", -1);
    }

    /**
     * Retrieves the original source line text by 1-based line number.
     */
    public String getSourceLine(int lineNumber) {
        if (lineNumber >= 1 && lineNumber <= sourceLines.length) {
            return sourceLines[lineNumber - 1];
        }
        return "";
    }

    /**
     * Scans all tokens until EOF.
     */
    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, currentLocation()));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '[':
                addToken(TokenType.LEFT_BRACKET);
                break;
            case ']':
                addToken(TokenType.RIGHT_BRACKET);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '+':
                addToken(match('=') ? TokenType.PLUS_ASSIGN : TokenType.PLUS);
                break;
            case '-':
                addToken(match('=') ? TokenType.MINUS_ASSIGN : TokenType.MINUS);
                break;
            case '*':
                addToken(match('=') ? TokenType.MULTIPLY_ASSIGN : TokenType.MULTIPLY);
                break;
            case '/':
                if (match('/')) {
                    // Line comment // ...
                    skipComment();
                } else if (match('=')) {
                    addToken(TokenType.DIVIDE_ASSIGN);
                } else {
                    addToken(TokenType.DIVIDE);
                }
                break;
            case '%':
                addToken(match('=') ? TokenType.MODULO_ASSIGN : TokenType.MODULO);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL : TokenType.ASSIGN);
                break;
            case '!':
                addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_THAN_EQUAL : TokenType.LESS_THAN);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_THAN_EQUAL : TokenType.GREATER_THAN);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    throw error("Unexpected character '&'. Did you mean '&&' or 'and'?");
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    throw error("Unexpected character '|'. Did you mean '||' or 'or'?");
                }
                break;
            case '#':
                // Hash comment # ...
                skipComment();
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore standard whitespace
                break;
            case '\n':
                line++;
                lineStartOffset = current;
                break;
            case '"':
                scanString();
                break;
            default:
                if (isDigit(c)) {
                    scanNumber();
                } else if (isAlpha(c)) {
                    scanIdentifierOrKeyword();
                } else {
                    throw error("Unexpected character '" + c + "'.");
                }
                break;
        }
    }

    private void skipComment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    private void scanString() {
        StringBuilder sb = new StringBuilder();
        SourceLocation stringStartLocation = currentLocation();

        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                lineStartOffset = current + 1;
            }
            char c = advance();
            if (c == '\\') {
                if (isAtEnd()) {
                    throw new LexerException("Unterminated string escape sequence.", currentLocation(), getSourceLine(line));
                }
                char escape = advance();
                switch (escape) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        sb.append(escape);
                        break;
                }
            } else {
                sb.append(c);
            }
        }

        if (isAtEnd()) {
            throw new LexerException("Unterminated string literal.", stringStartLocation, getSourceLine(stringStartLocation.getLine()));
        }

        // Consume the closing quote
        advance();

        String rawLexeme = source.substring(start, current);
        tokens.add(new Token(TokenType.STRING, rawLexeme, sb.toString(), stringStartLocation));
    }

    private void scanNumber() {
        while (isDigit(peek())) {
            advance();
        }

        boolean isDecimal = false;
        // Check for decimal part
        if (peek() == '.' && isDigit(peekNext())) {
            isDecimal = true;
            advance(); // consume '.'
            while (isDigit(peek())) {
                advance();
            }
        }

        String numStr = source.substring(start, current);
        if (isDecimal) {
            double val = Double.parseDouble(numStr);
            tokens.add(new Token(TokenType.DECIMAL, numStr, val, currentLocation()));
        } else {
            long val = Long.parseLong(numStr);
            if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                tokens.add(new Token(TokenType.INTEGER, numStr, (int) val, currentLocation()));
            } else {
                tokens.add(new Token(TokenType.INTEGER, numStr, val, currentLocation()));
            }
        }
    }

    private void scanIdentifierOrKeyword() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        String upperText = text.toUpperCase();

        // Check boolean literals
        if ("TRUE".equals(upperText)) {
            tokens.add(new Token(TokenType.BOOLEAN, text, Boolean.TRUE, currentLocation()));
            return;
        }
        if ("FALSE".equals(upperText)) {
            tokens.add(new Token(TokenType.BOOLEAN, text, Boolean.FALSE, currentLocation()));
            return;
        }

        // Check keywords
        TokenType keywordType = KEYWORDS.get(upperText);
        if (keywordType != null) {
            tokens.add(new Token(keywordType, text, null, currentLocation()));
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, text, null, currentLocation()));
        }
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private SourceLocation currentLocation() {
        int col = (start - lineStartOffset) + 1;
        return new SourceLocation(line, col);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, null, currentLocation()));
    }

    private LexerException error(String message) {
        return new LexerException(message, currentLocation(), getSourceLine(line));
    }
}
