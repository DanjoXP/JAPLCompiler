# JAPL Programming Language & Compiler

**JAPL** (*Java-targeted Pseudocode-inspired Language*) is an educational programming language compiler written in pure, modern Java with **zero third-party dependencies**.

JAPL source files use the `.japl` extension. The compiler translates `.japl` source code through a clean, multi-stage compiler pipeline into readable Java code, compiles the generated code to JVM bytecode (`.class`), and runs it directly on the JVM.

---

## Compiler Pipeline Architecture

The compiler is organized into clear, decoupled stages designed for educational clarity and easy extensibility:

```
                  +--------------------------------+
                  |       JAPL Source (.japl)      |
                  +--------------------------------+
                                  |
                                  v
                  +--------------------------------+
                  |         Lexer (Scanner)        |
                  |  Tokenizes text into Tokens    |
                  +--------------------------------+
                                  |
                                  v
                  +--------------------------------+
                  |      Parser (Recursive)        |
                  |  Builds Abstract Syntax Tree   |
                  +--------------------------------+
                                  |
                                  v
                  +--------------------------------+
                  |       Semantic Analyzer        |
                  |  Validates types & scopes      |
                  +--------------------------------+
                                  |
                                  v
                  +--------------------------------+
                  |      Java Code Generator       |
                  |  Emits readable Java source    |
                  +--------------------------------+
                                  |
                                  v
                  +--------------------------------+
                  |      Java Compiler Service     |
                  | Compiles to JVM Bytecode .class|
                  +--------------------------------+
                                  |
                                  v
                  +--------------------------------+
                  |         Program Runner         |
                  |    Executes bytecode on JVM    |
                  +--------------------------------+
```

### Directory Structure

```text
JAPLCompiler/
├── src/
│   └── com/japl/
│       ├── Main.java                          # CLI entry point
│       ├── common/
│       │   ├── SourceLocation.java            # Line & Column tracking
│       │   ├── CompilerException.java         # Visual caret error reporting
│       │   ├── LexerException.java            # Tokenization errors
│       │   ├── ParserException.java           # Syntax errors
│       │   └── SemanticException.java         # Type & scoping errors
│       ├── lexer/
│       │   ├── TokenType.java                 # Enum of all language tokens
│       │   ├── Token.java                     # Token data holder
│       │   └── Lexer.java                     # Scanner with comments & escape handling
│       ├── ast/
│       │   ├── ASTNode.java                   # AST node base class
│       │   ├── ProgramNode.java               # Root node containing functions
│       │   ├── FunctionNode.java              # Function declaration
│       │   ├── StatementNode.java             # Statement base class
│       │   ├── BlockNode.java                 # Block statement list
│       │   ├── AssignmentStatementNode.java   # Variable assignment & compound ops
│       │   ├── PrintStatementNode.java        # print(...) statement
│       │   ├── IfStatementNode.java           # if / else if / else conditional
│       │   ├── ReturnStatementNode.java       # return statement
│       │   ├── ExpressionStatementNode.java   # Expression as statement (e.g. calls)
│       │   ├── ExpressionNode.java            # Expression base class with type info
│       │   ├── BinaryExpressionNode.java      # Binary operators (+, -, *, ==, and, etc.)
│       │   ├── UnaryExpressionNode.java       # Unary operators (-, not, !)
│       │   ├── LiteralExpressionNode.java     # Int, Decimal, String, Boolean literals
│       │   ├── VariableExpressionNode.java    # Identifier reference
│       │   └── FunctionCallExpressionNode.java# Function invocation
│       ├── parser/
│       │   └── Parser.java                    # Recursive descent parser
│       ├── semantic/
│       │   ├── DataType.java                  # INT, DECIMAL, STRING, BOOLEAN, VOID, ANY
│       │   ├── Symbol.java                    # Variable & parameter metadata
│       │   ├── SymbolTable.java               # Scoped symbol table
│       │   └── SemanticAnalyzer.java          # Scoping and type checking
│       ├── codegen/
│       │   └── JavaCodeGenerator.java         # Pretty-printed Java emitter
│       ├── compiler/
│       │   └── JAPLCompiler.java              # End-to-end pipeline orchestrator
│       ├── runner/
│       │   ├── JavaCompilerService.java       # Compiles Java via ToolProvider or javac
│       │   └── ProgramRunner.java             # Runs compiled bytecode
│       └── test/
│           ├── JAPLTestRunner.java            # Zero-dependency test framework
│           ├── LexerTest.java                 # Tokenizer unit tests
│           ├── ParserTest.java                # Syntax and precedence tests
│           ├── SemanticAnalyzerTest.java      # Scoping and type tests
│           └── EndToEndTest.java              # Complete pipeline integration tests
├── examples/
│   ├── hello.japl                             # Simple Hello World program
│   ├── adult_check.japl                       # If/else branch from specification
│   ├── math_operations.japl                   # Operator precedence & compound assign
│   ├── comparisons.japl                       # English-readable operators demonstration
│   ├── functions.japl                         # Parameter passing & return values
│   └── error_demo.japl                        # Shows user-friendly error diagnostics
├── build.ps1                                  # Windows PowerShell build & test script
├── build.bat                                  # Windows Batch build & test script
└── README.md                                  # Documentation and language guide
```

---

## Language Syntax Guide

### 1. Functions
A function starts with `Begin Function <Name>(<parameters>)` and ends with `End Function`.
The `main()` function serves as the program entry point.

```text
Begin Function main()
    print("Welcome to JAPL")
End Function
```

Functions can accept comma-separated parameters and return values:

```text
Begin Function calculateArea(width, height)
    area = width * height
    return area
End Function

Begin Function main()
    total = calculateArea(10, 20)
    print("Area: " + total)
End Function
```

### 2. Variables & Type Inference
Variables are untyped in JAPL syntax. The compiler automatically infers Java-compatible types from expressions:
- **Integer**: e.g. `age = 30` (compiles to `int age = 30;`)
- **Decimal**: e.g. `rate = 3.14` (compiles to `double rate = 3.14;`)
- **String**: e.g. `name = "John"` (compiles to `String name = "John";`)
- **Boolean**: e.g. `active = true` (compiles to `boolean active = true;`)

Variables must be assigned before being used in expressions. Subsequent assignments re-assign the variable without re-declaring it.

### 3. Print Output
The built-in `print(...)` function outputs expressions to the console (compiles to `System.out.println(...)`):

```text
print("Hello World")
print(age)
print("Age in 10 years: " + (age + 10))
```

### 4. Conditionals
Conditional blocks use `if ... end if` or `if ... else ... end if` syntax. `else if` is also supported:

```text
if age < 18
    print("You are a minor")
else if age < 65
    print("You are an adult")
else
    print("You are a senior")
end if
```

### 5. Operators
JAPL supports both symbolic and English-readable operator syntax:

| Operation | Symbolic | Readable Keyword | Notes |
| :--- | :--- | :--- | :--- |
| Addition / Concat | `+` | | Arithmetic addition or String concatenation |
| Subtraction | `-` | | Arithmetic subtraction or unary negation |
| Multiplication | `*` | | Standard precedence higher than `+` / `-` |
| Division | `/` | | Standard division |
| Modulo | `%` | | Remainder operator |
| Assignment | `=` | | Variable declaration or reassignment |
| Compound Assign | `+=`, `-=`, `*=`, `/=`, `%=` | | Modifies existing variable |
| Equal To | `==` | `equalTo` | Compares numbers, booleans, or strings |
| Not Equal To | `!=` | `isNot` | Inequality comparison |
| Greater Than / Eq | `>=` | `greaterThan` | Can also use `greaterThanOrEqualTo` |
| Less Than / Eq | `<=` | `lessThan` | Can also use `lessThanOrEqualTo` |
| Greater Than | `>` | | Strict greater than |
| Less Than | `<` | | Strict less than |
| Logical AND | `&&` | `and` | Short-circuit boolean AND |
| Logical OR | `\|\|` | `or` | Short-circuit boolean OR |
| Logical NOT | `!` | `not` | Unary boolean negation |

Example with readable keywords:
```text
if age greaterThan 18 and active equalTo true
    print("Access Granted")
end if
```

### 6. Comments
Both hash (`#`) and double-slash (`//`) line comments are supported:

```text
# This is a comment
// This is also a comment
```

---

## Compiler Error Diagnostics

When syntax or semantic errors occur, the compiler prints user-friendly diagnostic messages showing the exact line, column, error description, and a visual pointer `^` to the error location:

```text
JAPL Compiler Error
Line 9, Column 1: Expected 'end if' before 'End Function'.

    End Function
    ^
```

---

## Building and Automatic Installation

Anyone who clones your repository can build and install JAPL in one click:

### Windows (One-Click Build & Install)
Simply double-click **`build.bat`** (or run it in Command Prompt / PowerShell):
```cmd
build.bat
```
This will:
1. Compile all Java sources
2. Run the 23 unit tests
3. Package `japl.jar`
4. **Automatically add JAPL to their Windows User PATH** so they can immediately type `japl` from any Command Prompt or Terminal!

To test the installation:
```cmd
japl -v
```
Output:
```text
JAPL Compiler version 1.0.0
```

### Windows (One-Click Uninstall)
Simply double-click **`uninstall.bat`** (or run it in Command Prompt):
```cmd
uninstall.bat
```
This will:
1. Automatically remove JAPL from the Windows User `PATH`
2. Clean up compiled `bin/`, `build/`, and `japl.jar` files
3. Keep the original source code clean and intact

*(You can reinstall at any time by running `build.bat` again)*

### Linux & macOS
```bash
chmod +x build.sh japl
./build.sh
```
To run globally on Unix:
```bash
sudo ln -s $(pwd)/japl /usr/local/bin/japl
```

### Running the Test Suite
The project contains an embedded, zero-dependency unit test framework:
```bash
java -cp bin com.japl.test.JAPLTestRunner
```

---

## CLI Usage

### 1. Compile and Run Directly (Default)
```bash
java -jar japl.jar examples/hello.japl
# Or explicitly:
java -jar japl.jar run examples/adult_check.japl
```

### 2. Inspect Generated Java Code
```bash
java -jar japl.jar java examples/math_operations.japl
```

Output:
```java
// Generated by JAPL Compiler
import java.util.Objects;

public class math_operations {
    public static void main(String[] args) {
        int a = 10;
        int b = 5;
        int c = 2;
        int result = (a + (b * c));
        System.out.println(("Result of 10 + 5 * 2 = " + result));
        int remainder = (17 % 5);
        System.out.println(("17 % 5 = " + remainder));
        int score = 100;
        score += 25;
        score -= 5;
        score *= 2;
        score /= 4;
        System.out.println(("Final score = " + score));
    }
}
```

### 3. Compile to `.java` and `.class` Bytecode
```bash
java -jar japl.jar compile examples/adult_check.japl -o build
```
Creates:
- `build/adult_check.java`
- `build/adult_check.class`
