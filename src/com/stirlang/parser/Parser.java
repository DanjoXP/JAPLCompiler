package com.stirlang.parser;

import com.stirlang.ast.*;
import com.stirlang.common.ParserException;
import com.stirlang.common.SourceLocation;
import com.stirlang.lexer.Lexer;
import com.stirlang.lexer.Token;
import com.stirlang.lexer.TokenType;
import com.stirlang.semantic.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recursive descent parser for the Stirlang programming language.
 *
 * Constructs a rich Abstract Syntax Tree (AST) while enforcing proper operator precedence
 * and generating friendly, educational compiler error messages.
 */
public class Parser {
    private final List<Token> tokens;
    private final Lexer lexer;
    private int current = 0;

    public Parser(List<Token> tokens, Lexer lexer) {
        this.tokens = tokens;
        this.lexer = lexer;
    }

    /**
     * Parses the full program into a ProgramNode.
     */
    public ProgramNode parse() {
        List<FunctionNode> functions = new ArrayList<>();
        List<StatementNode> topLevelStatements = new ArrayList<>();
        SourceLocation programStart = peek().getLocation();

        while (!isAtEnd()) {
            if (check(TokenType.BEGIN)) {
                functions.add(parseFunction());
            } else {
                topLevelStatements.add(parseStatement());
            }
        }

        boolean hasExplicitMain = false;
        for (FunctionNode fn : functions) {
            if (fn.isMain()) {
                hasExplicitMain = true;
                break;
            }
        }

        if (hasExplicitMain && !topLevelStatements.isEmpty()) {
            throw error(topLevelStatements.get(0).getLocation(),
                    "Cannot mix top-level statements with an explicit 'Begin Function main()'.");
        }

        if (!hasExplicitMain && !topLevelStatements.isEmpty()) {
            // Create synthetic main function containing top-level statements
            BlockNode mainBody = new BlockNode(topLevelStatements, topLevelStatements.get(0).getLocation());
            FunctionNode syntheticMain = new FunctionNode("main", java.util.Collections.emptyList(), mainBody, topLevelStatements.get(0).getLocation());
            functions.add(syntheticMain);
        }

        return new ProgramNode(functions, topLevelStatements, programStart);
    }

    // ==========================================
    // Function Parsing
    // ==========================================

    /**
     * Parses:
     * Begin Function FunctionName(param1, param2)
     *     ...
     * End Function
     */
    private FunctionNode parseFunction() {
        SourceLocation funcStart = peek().getLocation();

        if (!check(TokenType.BEGIN)) {
            throw error(peek(), "Expected 'Begin Function' declaration. Statements must be inside a function.");
        }
        advance(); // consume 'Begin'

        consume(TokenType.FUNCTION, "Expected 'Function' after 'Begin'.");

        Token nameToken = consume(TokenType.IDENTIFIER, "Expected function name after 'Begin Function'.");
        String functionName = nameToken.getLexeme();

        consume(TokenType.LEFT_PAREN, "Expected '(' after function name '" + functionName + "'.");
        List<String> parameters = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                Token paramToken = consume(TokenType.IDENTIFIER, "Expected parameter name in function declaration.");
                if (parameters.contains(paramToken.getLexeme())) {
                    throw error(paramToken, "Duplicate parameter name '" + paramToken.getLexeme() + "' in function '" + functionName + "'.");
                }
                parameters.add(paramToken.getLexeme());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after function parameters.");

        // Parse function body statements until 'End Function'
        List<StatementNode> bodyStatements = new ArrayList<>();
        while (!isAtEnd() && !isFunctionEnd()) {
            bodyStatements.add(parseStatement());
        }

        if (isAtEnd()) {
            throw error(funcStart, "Missing 'End Function' for function '" + functionName + "'.");
        }

        // Consume 'End Function'
        advance(); // consume 'End'
        consume(TokenType.FUNCTION, "Expected 'Function' after 'End' to close function '" + functionName + "'.");

        BlockNode body = new BlockNode(bodyStatements, funcStart);
        return new FunctionNode(functionName, parameters, body, funcStart);
    }

    private boolean isFunctionEnd() {
        return check(TokenType.END) && checkNext(TokenType.FUNCTION);
    }

    // ==========================================
    // Statement Parsing
    // ==========================================

    private StatementNode parseStatement() {
        if (check(TokenType.START) && checkNext(TokenType.LOOP)) {
            return parseLoopStatement();
        }
        if (check(TokenType.BREAK) && checkNext(TokenType.LOOP)) {
            return parseBreakStatement();
        }
        if (check(TokenType.CONTINUE) && checkNext(TokenType.LOOP)) {
            return parseContinueStatement();
        }
        if (check(TokenType.START)) {
            throw error(peek(), "Expected 'Loop' after 'Start'.");
        }
        if (check(TokenType.BREAK)) {
            throw error(peek(), "Expected 'Loop' after 'Break'.");
        }
        if (check(TokenType.CONTINUE)) {
            throw error(peek(), "Expected 'Loop' after 'Continue'.");
        }
        if (check(TokenType.PRINT)) {
            return parsePrintStatement();
        }
        if (check(TokenType.IF)) {
            return parseIfStatement();
        }
        if (check(TokenType.RETURN)) {
            return parseReturnStatement();
        }
        if (check(TokenType.IDENTIFIER)) {
            if (checkNext(TokenType.LEFT_PAREN)) {
                FunctionCallExpressionNode call = parseFunctionCall();
                return new ExpressionStatementNode(call, call.getLocation());
            } else if (isAssignmentOperator(peekNext().getType())) {
                return parseAssignmentStatement();
            }
        }

        // Detect misplaced 'end if', 'else', or 'End Loop'
        if (check(TokenType.END) && checkNext(TokenType.IF)) {
            throw error(peek(), "Unexpected 'end if' without matching 'if' statement.");
        }
        if (check(TokenType.END) && checkNext(TokenType.LOOP)) {
            throw error(peek(), "Unexpected 'End Loop' without matching 'Start Loop'.");
        }
        if (check(TokenType.ELSE)) {
            throw error(peek(), "Unexpected 'else' without matching 'if' statement.");
        }

        throw error(peek(), "Unexpected token '" + peek().getLexeme() + "'. Expected a statement (variable assignment, print, if, loop, or function call).");
    }

    private LoopStatementNode parseLoopStatement() {
        Token startToken = advance(); // consume 'Start'
        consume(TokenType.LOOP, "Expected 'Loop' after 'Start'.");
        SourceLocation loopLocation = startToken.getLocation();

        ExpressionNode countExpression = null;
        String counterVariable = null;

        if (match(TokenType.LEFT_PAREN)) {
            if (check(TokenType.RIGHT_PAREN)) {
                throw error(peek(), "Expected loop count expression inside 'Start Loop(...)'.");
            }
            countExpression = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after loop count expression.");

            if (match(TokenType.AS)) {
                Token varToken = consume(TokenType.IDENTIFIER, "Expected variable name after 'as' in loop declaration.");
                counterVariable = varToken.getLexeme();
            }
        } else if (match(TokenType.AS)) {
            throw error(previous(), "An infinite loop cannot declare a counter variable with 'as'. Use 'Start Loop(count) as <variable>'.");
        }

        // Parse loop body statements until 'End Loop'
        List<StatementNode> bodyStatements = new ArrayList<>();
        while (!isAtEnd() && !isLoopEnd()) {
            checkNotPrematureFunctionEndInLoop();
            checkNotPrematureIfEndInLoop();
            bodyStatements.add(parseStatement());
        }

        if (isAtEnd()) {
            throw error(loopLocation, "Expected 'End Loop' before end of file.");
        }

        // Consume 'End Loop'
        consume(TokenType.END, "Expected 'End Loop' to close loop.");
        consume(TokenType.LOOP, "Expected 'Loop' after 'End' to close loop.");

        BlockNode body = new BlockNode(bodyStatements, loopLocation);
        return new LoopStatementNode(countExpression, counterVariable, body, loopLocation);
    }

    private BreakStatementNode parseBreakStatement() {
        Token breakToken = advance(); // consume 'Break'
        consume(TokenType.LOOP, "Expected 'Loop' after 'Break'.");
        return new BreakStatementNode(breakToken.getLocation());
    }

    private ContinueStatementNode parseContinueStatement() {
        Token continueToken = advance(); // consume 'Continue'
        consume(TokenType.LOOP, "Expected 'Loop' after 'Continue'.");
        return new ContinueStatementNode(continueToken.getLocation());
    }

    private boolean isLoopEnd() {
        return check(TokenType.END) && checkNext(TokenType.LOOP);
    }

    private void checkNotPrematureFunctionEndInLoop() {
        if (isFunctionEnd()) {
            throw error(peek(), "Expected 'End Loop' before 'End Function'.");
        }
    }

    private void checkNotPrematureIfEndInLoop() {
        if (isIfEnd()) {
            throw error(peek(), "Unexpected 'end if' inside loop.");
        }
    }

    private PrintStatementNode parsePrintStatement() {
        Token printToken = advance(); // consume 'print'
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'print'.");
        ExpressionNode expr = parseExpression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after expression in 'print(...)'.");
        return new PrintStatementNode(expr, printToken.getLocation());
    }

    /**
     * Parses:
     * if condition
     *     statements
     * [else if condition
     *     statements]*
     * [else
     *     statements]
     * end if
     */
    private IfStatementNode parseIfStatement() {
        Token ifToken = advance(); // consume 'if'
        ExpressionNode condition = parseExpression();
        SourceLocation ifLocation = ifToken.getLocation();

        List<StatementNode> thenStatements = new ArrayList<>();
        List<IfStatementNode.ElseIfBranch> elseIfBranches = new ArrayList<>();
        BlockNode elseBranch = null;

        // Parse then branch until 'else', 'end if', or error
        while (!isAtEnd() && !isIfEnd() && !check(TokenType.ELSE)) {
            checkNotPrematureFunctionEnd();
            checkNotPrematureLoopEnd();
            thenStatements.add(parseStatement());
        }

        // Handle 'else if' or 'else'
        while (match(TokenType.ELSE)) {
            SourceLocation branchLocation = previous().getLocation();

            if (match(TokenType.IF)) {
                // Else If branch
                ExpressionNode elseIfCondition = parseExpression();
                List<StatementNode> elseIfStatements = new ArrayList<>();
                while (!isAtEnd() && !isIfEnd() && !check(TokenType.ELSE)) {
                    checkNotPrematureFunctionEnd();
                    checkNotPrematureLoopEnd();
                    elseIfStatements.add(parseStatement());
                }
                elseIfBranches.add(new IfStatementNode.ElseIfBranch(
                        elseIfCondition,
                        new BlockNode(elseIfStatements, branchLocation),
                        branchLocation
                ));
            } else {
                // Final Else branch
                List<StatementNode> elseStatements = new ArrayList<>();
                while (!isAtEnd() && !isIfEnd()) {
                    checkNotPrematureFunctionEnd();
                    checkNotPrematureLoopEnd();
                    if (check(TokenType.ELSE)) {
                        throw error(peek(), "Multiple 'else' blocks are not allowed in the same 'if' statement.");
                    }
                    elseStatements.add(parseStatement());
                }
                elseBranch = new BlockNode(elseStatements, branchLocation);
                break;
            }
        }

        if (isAtEnd()) {
            throw error(ifLocation, "Expected 'end if' before end of file.");
        }

        // Consume 'end if'
        consume(TokenType.END, "Expected 'end if' to close 'if' statement.");
        consume(TokenType.IF, "Expected 'if' after 'end' to close 'if' statement.");

        BlockNode thenBranch = new BlockNode(thenStatements, ifLocation);
        return new IfStatementNode(condition, thenBranch, elseIfBranches, elseBranch, ifLocation);
    }

    private boolean isIfEnd() {
        return check(TokenType.END) && checkNext(TokenType.IF);
    }

    private void checkNotPrematureFunctionEnd() {
        if (isFunctionEnd()) {
            throw error(peek(), "Expected 'end if' before 'End Function'.");
        }
    }

    private void checkNotPrematureLoopEnd() {
        if (isLoopEnd()) {
            throw error(peek(), "Expected 'end if' before 'End Loop'.");
        }
    }

    private AssignmentStatementNode parseAssignmentStatement() {
        Token varToken = advance(); // consume identifier
        Token opToken = advance();  // consume assignment operator (=, +=, -=, etc.)
        ExpressionNode value = parseExpression();
        return new AssignmentStatementNode(varToken.getLexeme(), opToken.getType(), value, varToken.getLocation());
    }

    private ReturnStatementNode parseReturnStatement() {
        Token returnToken = advance(); // consume 'return'
        ExpressionNode value = null;
        if (!isAtEnd() && !isFunctionEnd() && !isIfEnd() && !check(TokenType.ELSE)) {
            value = parseExpression();
        }
        return new ReturnStatementNode(value, returnToken.getLocation());
    }

    // ==========================================
    // Expression Parsing (Precedence Climbing)
    // ==========================================

    public ExpressionNode parseExpression() {
        return parseLogicalOr();
    }

    private ExpressionNode parseLogicalOr() {
        ExpressionNode left = parseLogicalAnd();

        while (match(TokenType.OR)) {
            Token op = previous();
            ExpressionNode right = parseLogicalAnd();
            left = new BinaryExpressionNode(left, op.getType(), right, op.getLocation());
        }

        return left;
    }

    private ExpressionNode parseLogicalAnd() {
        ExpressionNode left = parseEquality();

        while (match(TokenType.AND)) {
            Token op = previous();
            ExpressionNode right = parseEquality();
            left = new BinaryExpressionNode(left, op.getType(), right, op.getLocation());
        }

        return left;
    }

    private ExpressionNode parseEquality() {
        ExpressionNode left = parseRelational();

        while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            Token op = previous();
            ExpressionNode right = parseRelational();
            left = new BinaryExpressionNode(left, op.getType(), right, op.getLocation());
        }

        return left;
    }

    private ExpressionNode parseRelational() {
        ExpressionNode left = parseAdditive();

        while (match(TokenType.GREATER_THAN, TokenType.GREATER_THAN_EQUAL,
                     TokenType.LESS_THAN, TokenType.LESS_THAN_EQUAL)) {
            Token op = previous();
            ExpressionNode right = parseAdditive();
            left = new BinaryExpressionNode(left, op.getType(), right, op.getLocation());
        }

        return left;
    }

    private ExpressionNode parseAdditive() {
        ExpressionNode left = parseMultiplicative();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            ExpressionNode right = parseMultiplicative();
            left = new BinaryExpressionNode(left, op.getType(), right, op.getLocation());
        }

        return left;
    }

    private ExpressionNode parseMultiplicative() {
        ExpressionNode left = parseUnary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            Token op = previous();
            ExpressionNode right = parseUnary();
            left = new BinaryExpressionNode(left, op.getType(), right, op.getLocation());
        }

        return left;
    }

    private ExpressionNode parseUnary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token op = previous();
            ExpressionNode operand = parseUnary();
            return new UnaryExpressionNode(op.getType(), operand, op.getLocation());
        }

        return parsePrimary();
    }

    private ExpressionNode parsePrimary() {
        if (match(TokenType.INTEGER)) {
            return new LiteralExpressionNode(DataType.INT, previous().getLiteral(), previous().getLocation());
        }
        if (match(TokenType.DECIMAL)) {
            return new LiteralExpressionNode(DataType.DECIMAL, previous().getLiteral(), previous().getLocation());
        }
        if (match(TokenType.STRING)) {
            return new LiteralExpressionNode(DataType.STRING, previous().getLiteral(), previous().getLocation());
        }
        if (match(TokenType.BOOLEAN)) {
            return new LiteralExpressionNode(DataType.BOOLEAN, previous().getLiteral(), previous().getLocation());
        }
        if (check(TokenType.IDENTIFIER)) {
            if (checkNext(TokenType.LEFT_PAREN)) {
                return parseFunctionCall();
            }
            Token idToken = advance();
            return new VariableExpressionNode(idToken.getLexeme(), idToken.getLocation());
        }
        if (match(TokenType.LEFT_PAREN)) {
            SourceLocation loc = previous().getLocation();
            ExpressionNode expr = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after grouped expression.");
            return expr;
        }

        throw error(peek(), "Expected expression, but found '" + peek().getLexeme() + "'.");
    }

    private FunctionCallExpressionNode parseFunctionCall() {
        Token nameToken = consume(TokenType.IDENTIFIER, "Expected function name.");
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name.");

        List<ExpressionNode> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(parseExpression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments.");
        return new FunctionCallExpressionNode(nameToken.getLexeme(), arguments, nameToken.getLocation());
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().getType() == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        if (current + 1 >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(current + 1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAssignmentOperator(TokenType type) {
        return type == TokenType.ASSIGN ||
               type == TokenType.PLUS_ASSIGN ||
               type == TokenType.MINUS_ASSIGN ||
               type == TokenType.MULTIPLY_ASSIGN ||
               type == TokenType.DIVIDE_ASSIGN ||
               type == TokenType.MODULO_ASSIGN;
    }

    private ParserException error(Token token, String message) {
        String sourceLine = lexer != null ? lexer.getSourceLine(token.getLocation().getLine()) : "";
        return new ParserException(message, token.getLocation(), sourceLine);
    }

    private ParserException error(SourceLocation location, String message) {
        String sourceLine = lexer != null ? lexer.getSourceLine(location.getLine()) : "";
        return new ParserException(message, location, sourceLine);
    }
}
