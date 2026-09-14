# Stirlang Programming Language & Compiler

**Stirlang** is an educational, readable programming language compiler written in pure, modern Java with **zero third-party dependencies**.

Stirlang source files use the **`.stirl`** extension. The compiler translates `.stirl` source code through a clean, multi-stage compiler pipeline into readable Java code, compiles the generated code to JVM bytecode (`.class`), and runs it directly on the JVM.

---

## Compiler Pipeline Architecture

The compiler is organized into clear, decoupled stages designed for educational clarity and easy extensibility:

```
                +------------------------------------+
                |       Stirlang Source (.stirl)     |
                +------------------------------------+
                                  |
                                  v
                +------------------------------------+
                |          Lexer (Scanner)           |
                |     Tokenizes text into Tokens     |
                +------------------------------------+
                                  |
                                  v
                +------------------------------------+
                |         Parser (Recursive)         |
                |    Builds Abstract Syntax Tree     |
                +------------------------------------+
                                  |
                                  v
                +------------------------------------+
                |         Semantic Analyzer          |
                |    Validates types & scopes        |
                +------------------------------------+
                                  |
                                  v
                +------------------------------------+
                |        Java Code Generator         |
                |    Emits readable Java source      |
                +------------------------------------+
                                  |
                                  v
                +------------------------------------+
                |       Java Compiler Service        |
                |   Compiles to JVM Bytecode .class  |
                +------------------------------------+
                                  |
                                  v
                +------------------------------------+
                |           Program Runner           |
                |      Executes bytecode on JVM      |
                +------------------------------------+
```

### Directory Structure

```text
Stirlang/
├── src/
│   └── com/stirlang/
│       ├── Main.java                          # CLI entry point (stirlang / stirl)
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
│       │   ├── LoopStatementNode.java         # Loop statement (counted / infinite)
│       │   ├── BreakStatementNode.java        # break loop statement
│       │   ├── ContinueStatementNode.java     # continue loop statement
│       │   ├── AssignmentStatementNode.java   # Variable assignment & compound ops
│       │   ├── IndexAssignmentStatementNode.java # Array index assignment (a[0] = val)
│       │   ├── PrintStatementNode.java        # print(...) statement
│       │   ├── IfStatementNode.java           # if / else if / else conditional
│       │   ├── ReturnStatementNode.java       # return statement
│       │   ├── ExpressionStatementNode.java   # Expression as statement (e.g. calls)
│       │   ├── ExpressionNode.java            # Expression base class with type info
│       │   ├── BinaryExpressionNode.java      # Binary operators (+, -, *, ==, and, etc.)
│       │   ├── UnaryExpressionNode.java       # Unary operators (-, not, !)
│       │   ├── LiteralExpressionNode.java     # Int, Decimal, String, Boolean literals
│       │   ├── ArrayLiteralNode.java          # Array literal ({1, 2, 3})
│       │   ├── IndexAccessExpressionNode.java # Index access (a[0], a[-1], a[0][1])
│       │   ├── MethodCallExpressionNode.java  # Array methods (a.addToEnd(val))
│       │   ├── VariableExpressionNode.java    # Identifier reference
│       │   └── FunctionCallExpressionNode.java# Function invocation
│       ├── parser/
│       │   └── Parser.java                    # Recursive descent parser
│       ├── semantic/
│       │   ├── DataType.java                  # INT, DECIMAL, STRING, BOOLEAN, VOID, ARRAY, ANY
│       │   ├── Symbol.java                    # Variable & parameter metadata
│       │   ├── SymbolTable.java               # Scoped symbol table
│       │   └── SemanticAnalyzer.java          # Scoping and type checking
│       ├── runtime/
│       │   ├── StirlangArray.java             # Dynamic homogeneous array runtime representation
│       │   └── StirlangRuntimeError.java      # User-friendly runtime error handling
│       ├── codegen/
│       │   └── JavaCodeGenerator.java         # Pretty-printed Java emitter
│       ├── compiler/
│       │   └── StirlangCompiler.java          # End-to-end pipeline orchestrator
│       ├── runner/
│       │   ├── JavaCompilerService.java       # Compiles Java via ToolProvider or javac
│       │   └── ProgramRunner.java             # Runs compiled bytecode
│       └── test/
│           ├── StirlangTestRunner.java        # Zero-dependency test framework
│           ├── LexerTest.java                 # Tokenizer unit tests
│           ├── ParserTest.java                # Syntax and precedence tests
│           ├── SemanticAnalyzerTest.java      # Scoping and type tests
│           └── EndToEndTest.java              # Complete pipeline integration tests
├── examples/
│   ├── hello.stirl                            # Simple Hello World program
│   ├── adult_check.stirl                      # If/else branch demonstration
│   ├── math_operations.stirl                  # Operator precedence & compound assign
│   ├── comparisons.stirl                      # English-readable operators demonstration
│   ├── functions.stirl                        # Parameter passing & return values
│   ├── loops.stirl                            # Infinite, counted, break & continue loops
│   ├── arrays.stirl                           # Dynamic arrays, indexing, methods & nested arrays
│   ├── no_main_demo.stirl                     # Direct function call entry point
│   └── error_demo.stirl                       # User-friendly error diagnostics
├── install.bat                                # One-click build, test & install to User PATH
├── uninstall.bat                              # One-click uninstaller & cleanup
├── stirlang.bat                               # Windows CLI launcher
├── stirlang.jar                               # Pre-packaged executable JAR
└── README.md                                  # Complete language documentation
```

---

## Language Syntax Guide

### 1. Functions & Entry Points
A function starts with `begin function <Name>(<parameters>)` and ends with `end function`.

You **do not need a `main()` method** to run your program! Calling any function at the top level or running statements directly acts as the program entry point:

```text
begin function GreetPerson(name)
    print("Hello, " + name)
end function

# Access point: Top-level call runs automatically!
GreetPerson("Danny")
```

If a program contains functions but **no function is called** and **no `main()` function is defined**, the compiler will report:
```text
Stirlang Compiler Error
Line 1, Column 1: No Access Point. At least one function must be called, or 'begin function main()' must be defined.
```

Standard `begin function main()` blocks are also fully supported:
```text
begin function calculateArea(width, height)
    area = width * height
    return area
end function

begin function main()
    total = calculateArea(10, 20)
    print("Area: " + total)
end function
```

### 2. Variables & Type Inference
Variables are untyped in Stirlang syntax. The compiler automatically infers Java-compatible types from expressions:
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
Stirlang supports both symbolic and English-readable operator syntax:

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

### 6. Loops (`begin loop`, `end loop`, `break loop`, `continue loop`)

Stirlang provides clean and expressive loop constructs:

#### 1. Infinite Loop
```text
begin loop
    // Loop forever until a break loop is encountered
    break loop
end loop
```
Compiles to Java:
```java
while (true) {
    break;
}
```

#### 2. Counted Loop with Exposed Counter
```text
begin loop(10) as i
    print(i)
end loop
```
Runs 10 times, counting from `0` to `9`. The counter variable `i` is scoped exclusively to the loop body.
Compiles to Java:
```java
for (int i = 0; i < 10; i++) {
    System.out.println(i);
}
```

#### 3. Counted Loop without Counter
```text
begin loop(5)
    print("Repeating action")
end loop
```
Runs 5 times. Unique internal counters (`__loop0`, `__loop1`) are generated automatically so nested loops do not collide.
Compiles to Java:
```java
for (int __loop0 = 0; __loop0 < 5; __loop0++) {
    System.out.println("Repeating action");
}
```

#### 4. `break loop` and `continue loop`
- `break loop` immediately terminates the nearest enclosing loop (`break;`).
- `continue loop` immediately skips to the next iteration of the nearest enclosing loop (`continue;`).
- Using `break loop` or `continue loop` outside of a loop causes a compile-time error.

### 7. Dynamic Arrays

Stirlang features Python-style dynamic arrays with curly brace syntax `{...}`. Arrays are dynamically sized and homogeneous (all elements within an array must be of the same type).

#### 1. Declaration and Initialization
```text
# Initialized array (inferred element type: INT)
a = {1, 2, 3, 4}

# Empty array (element type is inferred on first element addition)
empty = {}
```

#### 2. Homogeneous Type Rules
All elements in an array must share the same type. Mixing types produces a clear compile-time or runtime error:
```text
# Invalid: compiler error
invalid = {1, "hello", 3}
```

#### 3. Indexing & Slicing
Arrays use zero-based indexing and support Python-style negative indexing:
```text
a = {10, 20, 30, 40}
print(a[0])    # Prints 10 (first element)
print(a[-1])   # Prints 40 (last element)
print(a[-2])   # Prints 30 (second from last)
```

Out-of-range positive or negative indices cleanly produce a Stirlang runtime error without exposing Java stack traces:
```text
Stirlang Runtime Error: Array index out of range: 5 (size: 4)
```

#### 4. Element Modification
Elements can be updated using indexed assignment (including negative indices):
```text
a = {1, 2, 3}
a[0] = 10
a[-1] = 99
print(a)   # Prints {10, 2, 99}
```

#### 5. Array Mutation Methods
Stirlang provides five built-in mutation methods on array instances:

| Method | Description | Example |
| :--- | :--- | :--- |
| `addToEnd(value)` | Appends value to the end of the array | `a.addToEnd(5)` |
| `addToFront(value)` | Prepends value to index 0 | `a.addToFront(0)` |
| `add(value, index)` | Inserts value at specified index (supports negative) | `a.add(99, 2)` |
| `remove(value)` | Removes **all** occurrences of value | `a.remove(2)` |
| `removeIndex(index)` | Removes element at specified index (supports negative) | `a.removeIndex(0)` |

#### 6. Nested Multidimensional Arrays
Arrays can be arbitrarily nested. The homogeneous type rule applies recursively:
```text
matrix = {{1, 2}, {3, 4}}
print(matrix[0][1])   # Prints 2
matrix[1][0] = 42
print(matrix)         # Prints {{1, 2}, {42, 4}}
```

#### 7. Clean Output Formatting
Printing an array with `print(...)` produces clean language-level syntax:
```text
a = {1, 2, 3}
print(a)   # Output: {1, 2, 3}
```

### 8. Comments
Both hash (`#`) and double-slash (`//`) line comments are supported:

```text
# This is a comment
// This is also a comment
```

---

## Compiler Error Diagnostics

When syntax or semantic errors occur, the compiler prints user-friendly diagnostic messages showing the exact line, column, error description, and a visual pointer `^` to the error location:

```text
Stirlang Compiler Error
Line 9, Column 1: Expected 'end if' before 'end function'.

    end function
    ^
```

---

## Installation and Setup

### Windows (One-Click Install)
Simply double-click **`install.bat`** (or run it in Command Prompt / PowerShell):
```cmd
install.bat
```
This will:
1. Compile all Java sources
2. Run all 71 unit and integration tests
3. Package `stirlang.jar`
4. **Automatically add Stirlang to your Windows User PATH** so you can immediately run `stirlang` from any Command Prompt or Terminal!

To test the installation:
```cmd
stirlang -v
```
Output:
```text
Stirlang Compiler version 1.0.0
```

### Windows (One-Click Uninstall)
Simply double-click **`uninstall.bat`** (or run it in Command Prompt):
```cmd
uninstall.bat
```
This will:
1. Automatically remove Stirlang from your Windows User `PATH`
2. Clean up compiled `bin/`, `build/`, and `stirlang.jar` files
3. Keep your source code clean and intact

*(To reinstall at any time, simply run `install.bat` again)*

---

## CLI Usage

### 1. Compile and Run Directly (Default)
```bash
stirlang examples/hello.stirl
# Or explicitly:
stirlang run examples/adult_check.stirl
```

### 2. Inspect Generated Java Code
```bash
stirlang java examples/math_operations.stirl
```

### 3. Compile to `.java` and `.class` Bytecode
```bash
stirlang compile examples/adult_check.stirl -o build
```
Creates:
- `build/adult_check.java`
- `build/adult_check.class`
