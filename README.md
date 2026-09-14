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
│   ├── no_main_demo.stirl                     # Direct function call entry point
│   └── error_demo.stirl                       # User-friendly error diagnostics
├── build.bat                                  # Windows CMD one-click build & install
├── build.ps1                                  # Windows PowerShell build & install
├── build.sh                                   # Linux / macOS build script
├── update.bat                                 # Windows CMD GitHub updater
├── update.ps1                                 # Windows PowerShell GitHub updater
├── update.sh                                  # Linux / macOS GitHub updater
├── uninstall.bat                              # One-click Windows uninstaller
├── uninstall.ps1                              # PowerShell uninstaller
├── uninstall.sh                               # Linux / macOS uninstaller
├── stirlang.bat / stirl.bat                   # Windows CMD CLI launchers
├── stirlang.cmd / stirl.cmd                   # Windows CMD alternative launchers
├── stirlang.ps1 / stirl.ps1                   # Windows PowerShell launchers
├── stirlang / stirl                           # Linux / macOS launchers
├── stirlang.jar                               # Pre-packaged executable JAR
└── README.md                                  # Complete language documentation
```

---

## Language Syntax Guide

### 1. Functions & Entry Points
A function starts with `Begin Function <Name>(<parameters>)` and ends with `End Function`.

You **do not need a `main()` method** to run your program! Calling any function at the top level or running statements directly acts as the program entry point:

```text
Begin Function GreetPerson(name)
    print("Hello, " + name)
End Function

# Access point: Top-level call runs automatically!
GreetPerson("Danny")
```

If a program contains functions but **no function is called** and **no `main()` function is defined**, the compiler will report:
```text
Stirlang Compiler Error
Line 1, Column 1: No Access Point. At least one function must be called, or 'Begin Function main()' must be defined.
```

Standard `Begin Function main()` blocks are also fully supported:
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

### 6. Loops (`Start Loop`, `End Loop`, `Break Loop`, `Continue Loop`)

Stirlang provides clean and expressive loop constructs:

#### 1. Infinite Loop
```text
Start Loop
    // Loop forever until a Break Loop is encountered
    Break Loop
End Loop
```
Compiles to Java:
```java
while (true) {
    break;
}
```

#### 2. Counted Loop with Exposed Counter
```text
Start Loop(10) as i
    print(i)
End Loop
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
Start Loop(5)
    print("Repeating action")
End Loop
```
Runs 5 times. Unique internal counters (`__loop0`, `__loop1`) are generated automatically so nested loops do not collide.
Compiles to Java:
```java
for (int __loop0 = 0; __loop0 < 5; __loop0++) {
    System.out.println("Repeating action");
}
```

#### 4. `Break Loop` and `Continue Loop`
- `Break Loop` immediately terminates the nearest enclosing loop (`break;`).
- `Continue Loop` immediately skips to the next iteration of the nearest enclosing loop (`continue;`).
- Using `Break Loop` or `Continue Loop` outside of a loop causes a compile-time error.

### 7. Comments
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
Line 9, Column 1: Expected 'end if' before 'End Function'.

    End Function
    ^
```

---

## Building and Automatic Installation

Anyone who clones your repository can build and install Stirlang in one click:

### Windows (One-Click Build & Install)
Simply double-click **`build.bat`** (or run it in Command Prompt / PowerShell):
```cmd
build.bat
```
This will:
1. Compile all Java sources
2. Run all 25 unit tests
3. Package `stirlang.jar`
4. **Automatically add Stirlang to their Windows User PATH** so they can immediately type `stirlang` or `stirl` from any Command Prompt or Terminal!

To test the installation:
```cmd
stirlang -v
```
Output:
```text
Stirlang Compiler version 1.0.0
```

### Updating Stirlang to the Latest Version
Whenever updates are pushed to GitHub, users can update their compiler with one command:
```cmd
stirlang -update
```
This automatically:
1. Connects to the GitHub repository
2. Fetches and pulls the latest source code
3. Recompiles and runs the test suite
4. Packages the new `stirlang.jar`
5. Confirms with `stirlang -v`

### Windows (One-Click Uninstall)
Simply double-click **`uninstall.bat`** (or run it in Command Prompt):
```cmd
uninstall.bat
```
This will:
1. Automatically remove Stirlang from the Windows User `PATH`
2. Clean up compiled `bin/`, `build/`, and `stirlang.jar` files
3. Keep the original source code clean and intact

*(You can reinstall at any time by running `build.bat` again)*

### Linux & macOS
```bash
chmod +x build.sh stirlang stirl update.sh uninstall.sh
./build.sh
```
To run globally on Unix:
```bash
sudo ln -s $(pwd)/stirlang /usr/local/bin/stirlang
```

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
