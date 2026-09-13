package com.japl.semantic;

import com.japl.ast.*;
import com.japl.common.SemanticException;
import com.japl.common.SourceLocation;
import com.japl.lexer.Lexer;
import com.japl.lexer.TokenType;

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

    public SemanticAnalyzer(Lexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Runs full semantic analysis on the given program AST.
     */
    public void analyze(ProgramNode program) {
        functionMap.clear();

        // Pass 1: Register all functions and check for duplicates
        for (FunctionNode fn : program.getFunctions()) {
            String nameLower = fn.getName().toLowerCase();
            if (functionMap.containsKey(nameLower)) {
                throw error(fn.getLocation(), "Duplicate function '" + fn.getName() + "' declared.");
            }
            functionMap.put(nameLower, fn);
        }

        // Check for required main() function
        if (!functionMap.containsKey("main")) {
            throw error(program.getLocation(), "Program is missing entry point 'Begin Function main()'.");
        }

        // Pass 2: Analyze function bodies
        for (FunctionNode fn : program.getFunctions()) {
            analyzeFunction(fn);
        }
    }

    private void analyzeFunction(FunctionNode function) {
        this.currentFunction = function;
        this.currentScope = new SymbolTable();

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
        } else if (stmt instanceof ReturnStatementNode) {
            analyzeReturn((ReturnStatementNode) stmt);
        } else if (stmt instanceof ExpressionStatementNode) {
            analyzeExpression(((ExpressionStatementNode) stmt).getExpression());
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
            currentScope.define(newSymbol);
        } else {
            // Re-assignment
            assign.setDeclaration(false);
            assign.setVariableType(existing.getType());

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
