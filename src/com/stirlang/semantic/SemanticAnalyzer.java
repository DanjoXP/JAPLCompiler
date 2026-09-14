package com.stirlang.semantic;

import com.stirlang.ast.*;
import com.stirlang.common.SemanticException;
import com.stirlang.common.SourceLocation;
import com.stirlang.lexer.Lexer;
import com.stirlang.lexer.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * Performs semantic validation on the AST:
 * - Checks for entry point (main function).
 * - Enforces lexical scoping and detects undeclared variables.
 * - Performs type inference on untyped variables.
 * - Type checks binary and unary operations, conditionals, and assignments.
 * - Validates function calls and argument counts.
 */
public class SemanticAnalyzer {
    private final Lexer lexer;
    private final Map<String, FunctionNode> functionMap = new HashMap<>();
    private SymbolTable currentScope;
    private FunctionNode currentFunction;
    private int loopDepth = 0;

    public SemanticAnalyzer(Lexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Runs full semantic analysis on the given program AST.
     */
    public void analyze(ProgramNode program) {
        functionMap.clear();
        loopDepth = 0;

        // Pass 1: Register all functions and check for duplicates
        for (FunctionNode fn : program.getFunctions()) {
            String nameLower = fn.getName().toLowerCase();
            if (functionMap.containsKey(nameLower)) {
                throw error(fn.getLocation(), "Duplicate function '" + fn.getName() + "' declared.");
            }
            functionMap.put(nameLower, fn);
        }

        // Check for required entry point
        if (!functionMap.containsKey("main")) {
            throw error(program.getLocation(), "No Access Point. At least one function must be called, or 'begin function main()' must be defined.");
        }

        // Pass 2: Analyze function bodies
        for (FunctionNode fn : program.getFunctions()) {
            analyzeFunction(fn);
        }
    }

    private void analyzeFunction(FunctionNode function) {
        this.currentFunction = function;
        this.currentScope = new SymbolTable();
        this.loopDepth = 0;

        // Register function parameters in function scope
        for (String paramName : function.getParameters()) {
            currentScope.define(new Symbol(paramName, DataType.ANY, function.getLocation(), true));
        }

        // Analyze function body
        analyzeBlock(function.getBody());
    }

    private void analyzeBlock(BlockNode block) {
        for (StatementNode stmt : block.getStatements()) {
            analyzeStatement(stmt);
        }
    }

    private void analyzeStatement(StatementNode stmt) {
        if (stmt instanceof AssignmentStatementNode) {
            analyzeAssignment((AssignmentStatementNode) stmt);
        } else if (stmt instanceof PrintStatementNode) {
            analyzePrint((PrintStatementNode) stmt);
        } else if (stmt instanceof IfStatementNode) {
            analyzeIf((IfStatementNode) stmt);
        } else if (stmt instanceof LoopStatementNode) {
            analyzeLoop((LoopStatementNode) stmt);
        } else if (stmt instanceof BreakStatementNode) {
            analyzeBreak((BreakStatementNode) stmt);
        } else if (stmt instanceof ContinueStatementNode) {
            analyzeContinue((ContinueStatementNode) stmt);
        } else if (stmt instanceof ReturnStatementNode) {
            analyzeReturn((ReturnStatementNode) stmt);
        } else if (stmt instanceof IndexAssignmentStatementNode) {
            analyzeIndexAssignment((IndexAssignmentStatementNode) stmt);
        } else if (stmt instanceof ExpressionStatementNode) {
            analyzeExpression(((ExpressionStatementNode) stmt).getExpression());
        }
    }

    private void analyzeLoop(LoopStatementNode loopStmt) {
        if (loopStmt.isCounted()) {
            analyzeExpression(loopStmt.getCountExpression());
            DataType countType = loopStmt.getCountExpression().getEvaluatedType();
            if (countType != DataType.ANY && !countType.isNumeric()) {
                throw error(loopStmt.getCountExpression().getLocation(),
                        "Loop count expression must evaluate to a number, but found " + countType + ".");
            }
        }

        SymbolTable previousScope = currentScope;
        currentScope = new SymbolTable(previousScope);
        loopDepth++;

        try {
            if (loopStmt.hasExposedCounter()) {
                String counterVar = loopStmt.getCounterVariable();
                if (previousScope.resolve(counterVar) != null) {
                    throw error(loopStmt.getLocation(),
                            "Loop counter variable '" + counterVar + "' is already declared in an enclosing scope.");
                }
                currentScope.define(new Symbol(counterVar, DataType.INT, loopStmt.getLocation(), false));
            }

            analyzeBlock(loopStmt.getBody());
        } finally {
            loopDepth--;
            currentScope = previousScope;
        }
    }

    private void analyzeBreak(BreakStatementNode breakStmt) {
        if (loopDepth <= 0) {
            throw error(breakStmt.getLocation(), "'break loop' cannot be used outside of a loop.");
        }
    }

    private void analyzeContinue(ContinueStatementNode continueStmt) {
        if (loopDepth <= 0) {
            throw error(continueStmt.getLocation(), "'continue loop' cannot be used outside of a loop.");
        }
    }

    private void analyzeAssignment(AssignmentStatementNode assign) {
        String varName = assign.getVariableName();
        ExpressionNode value = assign.getValue();
        analyzeExpression(value);

        DataType exprType = value.getEvaluatedType();
        Symbol existing = currentScope.resolve(varName);

        if (existing == null) {
            // First time this variable is assigned: it's a declaration!
            if (assign.getOperator() != TokenType.ASSIGN) {
                throw error(assign.getLocation(),
                        "Cannot use compound assignment '" + assign.getOperator().getRepresentation() +
                        "' on undefined variable '" + varName + "'.");
            }

            assign.setDeclaration(true);
            DataType varType = exprType != null ? exprType : DataType.ANY;
            assign.setVariableType(varType);

            Symbol newSymbol = new Symbol(varName, varType, assign.getLocation(), false);
            if (value instanceof ArrayLiteralNode) {
                ArrayLiteralNode lit = (ArrayLiteralNode) value;
                newSymbol.setArrayElementType(lit.getElementType());
                if (lit.getElementType() == DataType.ARRAY && !lit.getElements().isEmpty()) {
                    ExpressionNode first = lit.getElements().get(0);
                    if (first instanceof ArrayLiteralNode) {
                        newSymbol.setNestedArrayElementType(((ArrayLiteralNode) first).getElementType());
                    }
                }
            } else if (value instanceof VariableExpressionNode) {
                Symbol rhsSym = currentScope.resolve(((VariableExpressionNode) value).getName());
                if (rhsSym != null) {
                    newSymbol.setArrayElementType(rhsSym.getArrayElementType());
                    newSymbol.setNestedArrayElementType(rhsSym.getNestedArrayElementType());
                }
            }
            currentScope.define(newSymbol);
        } else {
            // Re-assignment
            assign.setDeclaration(false);
            assign.setVariableType(existing.getType());
            if (value instanceof ArrayLiteralNode) {
                ArrayLiteralNode lit = (ArrayLiteralNode) value;
                existing.setArrayElementType(lit.getElementType());
            }

            if (assign.getOperator() == TokenType.ASSIGN) {
                // Check type compatibility
                if (existing.getType() != DataType.ANY && exprType != DataType.ANY) {
                    if (!isTypeAssignable(existing.getType(), exprType)) {
                        throw error(assign.getLocation(),
                                "Cannot assign value of type " + exprType + " to variable '" +
                                varName + "' of type " + existing.getType() + ".");
                    }
                }
            } else {
                // Compound assignment checks
                if (existing.getType() != DataType.ANY && exprType != DataType.ANY) {
                    if (assign.getOperator() == TokenType.PLUS_ASSIGN) {
                        // '+=' allowed for numeric or string
                        boolean valid = (existing.getType().isNumeric() && exprType.isNumeric()) ||
                                        (existing.getType() == DataType.STRING);
                        if (!valid) {
                            throw error(assign.getLocation(),
                                    "Operator '+=' cannot be applied to types " + existing.getType() + " and " + exprType + ".");
                        }
                    } else {
                        // '-=', '*=', '/=', '%=' require numeric types
                        if (!existing.getType().isNumeric() || !exprType.isNumeric()) {
                            throw error(assign.getLocation(),
                                    "Operator '" + assign.getOperator().getRepresentation() +
                                    "' requires numeric types, but found " + existing.getType() + " and " + exprType + ".");
                        }
                    }
                }
            }
        }
    }

    private void analyzePrint(PrintStatementNode printStmt) {
        analyzeExpression(printStmt.getExpression());
    }

    private void analyzeIf(IfStatementNode ifStmt) {
        analyzeExpression(ifStmt.getCondition());
        DataType condType = ifStmt.getCondition().getEvaluatedType();

        if (condType != DataType.ANY && condType != DataType.BOOLEAN) {
            throw error(ifStmt.getCondition().getLocation(),
                    "Condition in 'if' statement must evaluate to a boolean, but found " + condType + ".");
        }

        analyzeBlock(ifStmt.getThenBranch());

        for (IfStatementNode.ElseIfBranch branch : ifStmt.getElseIfBranches()) {
            analyzeExpression(branch.getCondition());
            DataType branchCondType = branch.getCondition().getEvaluatedType();
            if (branchCondType != DataType.ANY && branchCondType != DataType.BOOLEAN) {
                throw error(branch.getCondition().getLocation(),
                        "Condition in 'else if' must evaluate to a boolean, but found " + branchCondType + ".");
            }
            analyzeBlock(branch.getBody());
        }

        if (ifStmt.getElseBranch() != null) {
            analyzeBlock(ifStmt.getElseBranch());
        }
    }

    private void analyzeReturn(ReturnStatementNode returnStmt) {
        if (currentFunction.isMain()) {
            if (returnStmt.getValue() != null) {
                throw error(returnStmt.getLocation(), "The 'main' function cannot return a value.");
            }
            return;
        }

        if (returnStmt.getValue() != null) {
            analyzeExpression(returnStmt.getValue());
            DataType retType = returnStmt.getValue().getEvaluatedType();
            if (currentFunction.getReturnType() == DataType.VOID) {
                currentFunction.setReturnType(retType);
            } else if (currentFunction.getReturnType() != retType && currentFunction.getReturnType() != DataType.ANY) {
                currentFunction.setReturnType(DataType.ANY);
            }
        }
    }

    private void analyzeExpression(ExpressionNode expr) {
        if (expr instanceof LiteralExpressionNode) {
            // Evaluated type is already set on literal
        } else if (expr instanceof VariableExpressionNode) {
            VariableExpressionNode var = (VariableExpressionNode) expr;
            Symbol symbol = currentScope.resolve(var.getName());
            if (symbol == null) {
                throw error(var.getLocation(), "Variable '" + var.getName() + "' is used before being defined.");
            }
            var.setEvaluatedType(symbol.getType());
        } else if (expr instanceof UnaryExpressionNode) {
            analyzeUnary((UnaryExpressionNode) expr);
        } else if (expr instanceof BinaryExpressionNode) {
            analyzeBinary((BinaryExpressionNode) expr);
        } else if (expr instanceof FunctionCallExpressionNode) {
            analyzeFunctionCall((FunctionCallExpressionNode) expr);
        } else if (expr instanceof ArrayLiteralNode) {
            analyzeArrayLiteral((ArrayLiteralNode) expr);
        } else if (expr instanceof IndexAccessExpressionNode) {
            analyzeIndexAccess((IndexAccessExpressionNode) expr);
        } else if (expr instanceof MethodCallExpressionNode) {
            analyzeMethodCall((MethodCallExpressionNode) expr);
        }
    }

    private void analyzeUnary(UnaryExpressionNode unary) {
        analyzeExpression(unary.getOperand());
        DataType operandType = unary.getOperand().getEvaluatedType();

        if (unary.getOperator() == TokenType.NOT) {
            if (operandType != DataType.ANY && operandType != DataType.BOOLEAN) {
                throw error(unary.getLocation(), "Operator 'not' requires a boolean operand, found " + operandType + ".");
            }
            unary.setEvaluatedType(DataType.BOOLEAN);
        } else if (unary.getOperator() == TokenType.MINUS) {
            if (operandType != DataType.ANY && !operandType.isNumeric()) {
                throw error(unary.getLocation(), "Unary '-' requires a numeric operand, found " + operandType + ".");
            }
            unary.setEvaluatedType(operandType == DataType.DECIMAL ? DataType.DECIMAL : DataType.INT);
        }
    }

    private void analyzeBinary(BinaryExpressionNode binary) {
        analyzeExpression(binary.getLeft());
        analyzeExpression(binary.getRight());

        DataType leftType = binary.getLeft().getEvaluatedType();
        DataType rightType = binary.getRight().getEvaluatedType();
        TokenType op = binary.getOperator();

        switch (op) {
            case PLUS:
                if (leftType == DataType.STRING || rightType == DataType.STRING) {
                    binary.setEvaluatedType(DataType.STRING);
                } else if (leftType.isNumeric() && rightType.isNumeric()) {
                    if (leftType == DataType.DECIMAL || rightType == DataType.DECIMAL) {
                        binary.setEvaluatedType(DataType.DECIMAL);
                    } else {
                        binary.setEvaluatedType(DataType.INT);
                    }
                } else if (leftType == DataType.ANY || rightType == DataType.ANY) {
                    binary.setEvaluatedType(DataType.ANY);
                } else {
                    throw error(binary.getLocation(), "Operator '+' is not supported between " + leftType + " and " + rightType + ".");
                }
                break;

            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                if (leftType != DataType.ANY && !leftType.isNumeric()) {
                    throw error(binary.getLeft().getLocation(), "Operator '" + op.getRepresentation() + "' requires numeric operand, found " + leftType + ".");
                }
                if (rightType != DataType.ANY && !rightType.isNumeric()) {
                    throw error(binary.getRight().getLocation(), "Operator '" + op.getRepresentation() + "' requires numeric operand, found " + rightType + ".");
                }

                if (leftType == DataType.DECIMAL || rightType == DataType.DECIMAL) {
                    binary.setEvaluatedType(DataType.DECIMAL);
                } else {
                    binary.setEvaluatedType(DataType.INT);
                }
                break;

            case GREATER_THAN:
            case GREATER_THAN_EQUAL:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
                if (leftType != DataType.ANY && !leftType.isNumeric()) {
                    throw error(binary.getLeft().getLocation(), "Comparison operator requires numeric operand, found " + leftType + ".");
                }
                if (rightType != DataType.ANY && !rightType.isNumeric()) {
                    throw error(binary.getRight().getLocation(), "Comparison operator requires numeric operand, found " + rightType + ".");
                }
                binary.setEvaluatedType(DataType.BOOLEAN);
                break;

            case EQUAL:
            case NOT_EQUAL:
                binary.setEvaluatedType(DataType.BOOLEAN);
                break;

            case AND:
            case OR:
                if (leftType != DataType.ANY && leftType != DataType.BOOLEAN) {
                    throw error(binary.getLeft().getLocation(), "Logical operator requires boolean operand, found " + leftType + ".");
                }
                if (rightType != DataType.ANY && rightType != DataType.BOOLEAN) {
                    throw error(binary.getRight().getLocation(), "Logical operator requires boolean operand, found " + rightType + ".");
                }
                binary.setEvaluatedType(DataType.BOOLEAN);
                break;

            default:
                binary.setEvaluatedType(DataType.ANY);
                break;
        }
    }

    private void analyzeFunctionCall(FunctionCallExpressionNode call) {
        String nameLower = call.getFunctionName().toLowerCase();
        FunctionNode target = functionMap.get(nameLower);

        if (target == null) {
            throw error(call.getLocation(), "Call to undefined function '" + call.getFunctionName() + "'.");
        }

        int expectedArgs = target.getParameters().size();
        int actualArgs = call.getArguments().size();
        if (expectedArgs != actualArgs) {
            throw error(call.getLocation(),
                    "Function '" + call.getFunctionName() + "' expects " + expectedArgs +
                    " arguments, but " + actualArgs + " were provided.");
        }

        for (int i = 0; i < call.getArguments().size(); i++) {
            ExpressionNode arg = call.getArguments().get(i);
            analyzeExpression(arg);
            if (i < target.getParameters().size()) {
                String paramName = target.getParameters().get(i);
                DataType currentType = target.getParameterTypes().get(paramName);
                if ((currentType == null || currentType == DataType.ANY) &&
                    arg.getEvaluatedType() != null &&
                    arg.getEvaluatedType() != DataType.ANY) {
                    target.setParameterType(paramName, arg.getEvaluatedType());
                }
            }
        }

        call.setEvaluatedType(target.getReturnType());
    }

    private void analyzeIndexAssignment(IndexAssignmentStatementNode assign) {
        IndexAccessExpressionNode target = assign.getTarget();
        analyzeIndexAccess(target);
        analyzeExpression(assign.getValue());

        DataType valType = assign.getValue().getEvaluatedType();
        DataType targetElemType = target.getElementType();

        if (targetElemType != null && targetElemType != DataType.ANY && valType != null && valType != DataType.ANY) {
            if (!isTypeAssignable(targetElemType, valType)) {
                throw error(assign.getLocation(),
                        "Cannot assign value of type " + valType + " to array element of type " + targetElemType + ".");
            }
        }
    }

    private void analyzeArrayLiteral(ArrayLiteralNode arrayLiteral) {
        java.util.List<ExpressionNode> elements = arrayLiteral.getElements();
        DataType elementType = DataType.ANY;

        if (!elements.isEmpty()) {
            for (ExpressionNode elem : elements) {
                analyzeExpression(elem);
            }

            DataType firstConcreteType = null;
            for (ExpressionNode elem : elements) {
                if (elem.getEvaluatedType() != null && elem.getEvaluatedType() != DataType.ANY) {
                    firstConcreteType = elem.getEvaluatedType();
                    break;
                }
            }

            if (firstConcreteType != null) {
                for (ExpressionNode elem : elements) {
                    DataType t = elem.getEvaluatedType();
                    if (t != null && t != DataType.ANY && t != firstConcreteType) {
                        throw error(elem.getLocation(),
                                "Array element type mismatch: all elements must have the same type, but found " +
                                firstConcreteType + " and " + t + ".");
                    }
                    if (elem instanceof ArrayLiteralNode && firstConcreteType == DataType.ARRAY) {
                        ArrayLiteralNode sub = (ArrayLiteralNode) elem;
                        if (sub.getElementType() != null && sub.getElementType() != DataType.ANY) {
                            for (ExpressionNode other : elements) {
                                if (other instanceof ArrayLiteralNode && other != sub) {
                                    ArrayLiteralNode otherSub = (ArrayLiteralNode) other;
                                    if (otherSub.getElementType() != null && otherSub.getElementType() != DataType.ANY &&
                                            otherSub.getElementType() != sub.getElementType()) {
                                        throw error(other.getLocation(),
                                                "Nested array element type mismatch: found array of " +
                                                sub.getElementType() + " and array of " + otherSub.getElementType() + ".");
                                    }
                                }
                            }
                        }
                    }
                }
                elementType = firstConcreteType;
            }
        }

        arrayLiteral.setElementType(elementType);
        arrayLiteral.setEvaluatedType(DataType.ARRAY);
    }

    private void analyzeIndexAccess(IndexAccessExpressionNode indexAccess) {
        analyzeExpression(indexAccess.getTarget());
        analyzeExpression(indexAccess.getIndex());

        DataType targetType = indexAccess.getTarget().getEvaluatedType();
        DataType indexType = indexAccess.getIndex().getEvaluatedType();

        if (targetType != DataType.ANY && targetType != DataType.ARRAY) {
            throw error(indexAccess.getLocation(), "Cannot index into non-array value of type " + targetType + ".");
        }

        if (indexType != DataType.ANY && !indexType.isNumeric()) {
            throw error(indexAccess.getIndex().getLocation(), "Array index must be an integer, but found " + indexType + ".");
        }

        DataType elemType = DataType.ANY;
        if (indexAccess.getTarget() instanceof ArrayLiteralNode) {
            elemType = ((ArrayLiteralNode) indexAccess.getTarget()).getElementType();
        } else if (indexAccess.getTarget() instanceof VariableExpressionNode) {
            String varName = ((VariableExpressionNode) indexAccess.getTarget()).getName();
            Symbol sym = currentScope.resolve(varName);
            if (sym != null && sym.getArrayElementType() != null) {
                elemType = sym.getArrayElementType();
            }
        } else if (indexAccess.getTarget() instanceof IndexAccessExpressionNode) {
            IndexAccessExpressionNode parent = (IndexAccessExpressionNode) indexAccess.getTarget();
            if (parent.getElementType() == DataType.ARRAY) {
                if (parent.getTarget() instanceof VariableExpressionNode) {
                    Symbol sym = currentScope.resolve(((VariableExpressionNode) parent.getTarget()).getName());
                    if (sym != null && sym.getNestedArrayElementType() != null) {
                        elemType = sym.getNestedArrayElementType();
                    }
                }
            } else {
                elemType = parent.getElementType();
            }
        }
        indexAccess.setElementType(elemType);
        indexAccess.setEvaluatedType(elemType != null ? elemType : DataType.ANY);
    }

    private void analyzeMethodCall(MethodCallExpressionNode call) {
        analyzeExpression(call.getTarget());
        for (ExpressionNode arg : call.getArguments()) {
            analyzeExpression(arg);
        }

        DataType targetType = call.getTarget().getEvaluatedType();
        if (targetType != DataType.ANY && targetType != DataType.ARRAY) {
            throw error(call.getLocation(), "Cannot call method '" + call.getMethodName() + "' on non-array value of type " + targetType + ".");
        }

        String name = call.getMethodName();
        java.util.List<ExpressionNode> args = call.getArguments();

        switch (name) {
            case "addToEnd":
            case "addToFront": {
                if (args.size() != 1) {
                    throw error(call.getLocation(), "Method '" + name + "' expects 1 argument, but found " + args.size() + ".");
                }
                break;
            }
            case "add": {
                if (args.size() != 2) {
                    throw error(call.getLocation(), "Method 'add' expects 2 arguments (value, index), but found " + args.size() + ".");
                }
                DataType idxType = args.get(1).getEvaluatedType();
                if (idxType != DataType.ANY && !idxType.isNumeric()) {
                    throw error(args.get(1).getLocation(), "Method 'add' second argument (index) must be an integer, but found " + idxType + ".");
                }
                break;
            }
            case "remove": {
                if (args.size() != 1) {
                    throw error(call.getLocation(), "Method 'remove' expects 1 argument (value), but found " + args.size() + ".");
                }
                break;
            }
            case "removeIndex": {
                if (args.size() != 1) {
                    throw error(call.getLocation(), "Method 'removeIndex' expects 1 argument (index), but found " + args.size() + ".");
                }
                DataType idxType = args.get(0).getEvaluatedType();
                if (idxType != DataType.ANY && !idxType.isNumeric()) {
                    throw error(args.get(0).getLocation(), "Method 'removeIndex' argument (index) must be an integer, but found " + idxType + ".");
                }
                break;
            }
            default:
                throw error(call.getLocation(), "Unknown array method '" + name + "'. Valid methods: addToEnd, addToFront, add, remove, removeIndex.");
        }

        call.setEvaluatedType(DataType.VOID);
    }

    private boolean isTypeAssignable(DataType target, DataType source) {
        if (target == source) return true;
        if (target == DataType.ANY || source == DataType.ANY) return true;
        // int can be assigned to decimal (double)
        return target == DataType.DECIMAL && source == DataType.INT;
    }

    private SemanticException error(SourceLocation location, String message) {
        String sourceLine = lexer != null ? lexer.getSourceLine(location.getLine()) : "";
        return new SemanticException(message, location, sourceLine);
    }
}
