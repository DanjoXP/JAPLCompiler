package com.stirlang.test;

import com.stirlang.compiler.StirlangCompiler;
import com.stirlang.runner.ProgramRunner;

import static com.stirlang.test.StirlangTestRunner.*;

public class EndToEndTest {

    public void testHelloWorld() throws Exception {
        String code = "begin function main()\n" +
                      "    print(\"Hello World\")\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestHello");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Hello World"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testAdultCheck() throws Exception {
        String code = "begin function main()\n" +
                      "    age = 30\n" +
                      "    if age < 18\n" +
                      "        print(\"Hello World\")\n" +
                      "    else\n" +
                      "        print(\"You are an adult\")\n" +
                      "    end if\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestAdultCheck");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("You are an adult"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testMathPrecedenceAndCompoundAssign() throws Exception {
        String code = "begin function main()\n" +
                      "    result = 10 + 5 * 2\n" +
                      "    result += 10\n" +
                      "    print(result)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestMath");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("30"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testReadableComparisonsAndBooleans() throws Exception {
        String code = "begin function main()\n" +
                      "    age = 20\n" +
                      "    active = true\n" +
                      "    if age greaterThan 18 and active equalTo true\n" +
                      "        print(\"Eligible\")\n" +
                      "    end if\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestReadableComparisons");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Eligible"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testFunctionCall() throws Exception {
        String code = "begin function greet(name)\n" +
                      "    print(\"Hello \" + name)\n" +
                      "end function\n\n" +
                      "begin function main()\n" +
                      "    greet(\"Alice\")\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestFunctionCall");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Hello Alice"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testTopLevelFunctionCallWithoutMain() throws Exception {
        String code = "begin function GreetPerson(name)\n" +
                      "    print(\"Hello, \" + name)\n" +
                      "end function\n\n" +
                      "GreetPerson(\"Danny\")";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestGreetDanny");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Hello, Danny"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testCountedLoopWithExposedCounter() throws Exception {
        String code = "begin function main()\n" +
                      "    begin loop(5) as i\n" +
                      "        print(i)\n" +
                      "    end loop\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestLoopExposed");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        String[] lines = result.getOutput().trim().split("\\r?\\n");
        assertEquals(5, lines.length);
        assertEquals("0", lines[0]);
        assertEquals("1", lines[1]);
        assertEquals("2", lines[2]);
        assertEquals("3", lines[3]);
        assertEquals("4", lines[4]);
    }

    public void testCountedLoopWithoutExposedCounter() throws Exception {
        String code = "begin function main()\n" +
                      "    total = 0\n" +
                      "    begin loop(4)\n" +
                      "        total += 2\n" +
                      "    end loop\n" +
                      "    print(total)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestLoopUnexposed");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("8"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testNestedLoops() throws Exception {
        String code = "begin function main()\n" +
                      "    begin loop(2) as outer\n" +
                      "        begin loop(3) as inner\n" +
                      "            print(outer + \":\" + inner)\n" +
                      "        end loop\n" +
                      "    end loop\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestNestedLoops");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        String[] lines = result.getOutput().trim().split("\\r?\\n");
        assertEquals(6, lines.length);
        assertEquals("0:0", lines[0]);
        assertEquals("0:1", lines[1]);
        assertEquals("0:2", lines[2]);
        assertEquals("1:0", lines[3]);
        assertEquals("1:1", lines[4]);
        assertEquals("1:2", lines[5]);
    }

    public void testBreakAndContinue() throws Exception {
        String code = "begin function main()\n" +
                      "    begin loop(10) as i\n" +
                      "        if i == 2\n" +
                      "            continue loop\n" +
                      "        end if\n" +
                      "        if i == 5\n" +
                      "            break loop\n" +
                      "        end if\n" +
                      "        print(i)\n" +
                      "    end loop\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestBreakContinue");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        String[] lines = result.getOutput().trim().split("\\r?\\n");
        assertEquals(4, lines.length);
        assertEquals("0", lines[0]);
        assertEquals("1", lines[1]);
        assertEquals("3", lines[2]);
        assertEquals("4", lines[3]);
    }

    public void testInfiniteLoopWithBreak() throws Exception {
        String code = "begin function main()\n" +
                      "    n = 0\n" +
                      "    begin loop\n" +
                      "        n += 1\n" +
                      "        if n == 3\n" +
                      "            break loop\n" +
                      "        end if\n" +
                      "    end loop\n" +
                      "    print(n)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestInfiniteBreak");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("3"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testTopLevelLoop() throws Exception {
        String code = "total = 0\n" +
                      "begin loop(5) as idx\n" +
                      "    total += idx\n" +
                      "end loop\n" +
                      "print(total)";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestTopLevelLoop");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("10"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testExecutionTimeFormatting() {
        assertEquals("hello.stirl executed in 1 millisecond",
                com.stirlang.Main.formatExecutionTime("hello.stirl", 1_000_000L));
        assertEquals("hello.stirl executed in 45 milliseconds",
                com.stirlang.Main.formatExecutionTime("hello.stirl", 45_000_000L));
        assertEquals("hello.stirl executed in 0 milliseconds",
                com.stirlang.Main.formatExecutionTime("hello.stirl", 0L));
        assertEquals("calc.stirl executed in 1.50 seconds",
                com.stirlang.Main.formatExecutionTime("calc.stirl", 1_500_000_000L));
        assertEquals("loops.stirl executed in 2.05 seconds",
                com.stirlang.Main.formatExecutionTime("loops.stirl", 2_050_000_000L));
    }

    public void testArrayCreationAndPrinting() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3, 4}\n" +
                      "    print(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestArrayPrint");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertEquals("{1, 2, 3, 4}", result.getOutput().trim());
    }

    public void testEmptyArray() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {}\n" +
                      "    print(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestEmptyArray");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertEquals("{}", result.getOutput().trim());
    }

    public void testPositiveAndNegativeIndexing() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {10, 20, 30}\n" +
                      "    print(a[0])\n" +
                      "    print(a[-1])\n" +
                      "    print(a[-2])\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestArrayIndexing");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        String[] lines = result.getOutput().trim().split("\\r?\\n");
        assertEquals(3, lines.length);
        assertEquals("10", lines[0]);
        assertEquals("30", lines[1]);
        assertEquals("20", lines[2]);
    }

    public void testIndexAssignment() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3}\n" +
                      "    a[0] = 10\n" +
                      "    a[-1] = 30\n" +
                      "    print(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestIndexAssign");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertEquals("{10, 2, 30}", result.getOutput().trim());
    }

    public void testNestedArraysAndChainedAccess() throws Exception {
        String code = "begin function main()\n" +
                      "    m = {{1, 2}, {3, 4}}\n" +
                      "    print(m)\n" +
                      "    print(m[0][1])\n" +
                      "    m[1][0] = 99\n" +
                      "    print(m)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestNestedArrays");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        String[] lines = result.getOutput().trim().split("\\r?\\n");
        assertEquals(3, lines.length);
        assertEquals("{{1, 2}, {3, 4}}", lines[0]);
        assertEquals("2", lines[1]);
        assertEquals("{{1, 2}, {99, 4}}", lines[2]);
    }

    public void testArrayMethods() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3}\n" +
                      "    a.addToEnd(4)\n" +
                      "    print(a)\n" +
                      "    a.addToFront(0)\n" +
                      "    print(a)\n" +
                      "    a.add(99, 2)\n" +
                      "    print(a)\n" +
                      "    a.remove(99)\n" +
                      "    print(a)\n" +
                      "    a.removeIndex(0)\n" +
                      "    print(a)\n" +
                      "    a.removeIndex(-1)\n" +
                      "    print(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestArrayMethods");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        String[] lines = result.getOutput().trim().split("\\r?\\n");
        assertEquals(6, lines.length);
        assertEquals("{1, 2, 3, 4}", lines[0]);
        assertEquals("{0, 1, 2, 3, 4}", lines[1]);
        assertEquals("{0, 1, 99, 2, 3, 4}", lines[2]);
        assertEquals("{0, 1, 2, 3, 4}", lines[3]);
        assertEquals("{1, 2, 3, 4}", lines[4]);
        assertEquals("{1, 2, 3}", lines[5]);
    }

    public void testEmptyArrayTypeInferenceOnFirstAdd() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {}\n" +
                      "    a.addToEnd(42)\n" +
                      "    a.addToEnd(100)\n" +
                      "    print(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestEmptyArrayAdd");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertEquals("{42, 100}", result.getOutput().trim());
    }

    public void testRemoveMultipleOccurrences() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3, 2, 4, 2}\n" +
                      "    a.remove(2)\n" +
                      "    print(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestRemoveMulti");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertEquals("{1, 3, 4}", result.getOutput().trim());
    }

    public void testRemoveNonExistentElement() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3}\n" +
                      "    a.remove(99)\n" +
                      "    print(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestRemoveNonExistent");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertEquals("{1, 2, 3}", result.getOutput().trim());
    }

    public void testArrayEquality() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3}\n" +
                      "    b = {1, 2, 3}\n" +
                      "    c = {1, 2, 4}\n" +
                      "    if a == b\n" +
                      "        print(\"a equals b\")\n" +
                      "    end if\n" +
                      "    if a != c\n" +
                      "        print(\"a not equals c\")\n" +
                      "    end if\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestArrayEquality");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        String[] lines = result.getOutput().trim().split("\\r?\\n");
        assertEquals(2, lines.length);
        assertEquals("a equals b", lines[0]);
        assertEquals("a not equals c", lines[1]);
    }

    public void testArrayOutOfRangePositive() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3}\n" +
                      "    print(a[5])\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestOutOfRangePos");

        assertEquals(1, result.getExitCode(), "Program should exit with code 1");
        assertTrue(result.getError().contains("Stirlang Runtime Error: Array index out of range: 5 (size: 3)"),
                "Error output was: " + result.getError());
    }

    public void testArrayOutOfRangeNegative() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3}\n" +
                      "    print(a[-4])\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestOutOfRangeNeg");

        assertEquals(1, result.getExitCode(), "Program should exit with code 1");
        assertTrue(result.getError().contains("Stirlang Runtime Error: Array index out of range: -4 (size: 3)"),
                "Error output was: " + result.getError());
    }

    public void testRuntimeTypeMismatchOnAdd() throws Exception {
        String code = "begin function main()\n" +
                      "    a = {1, 2, 3}\n" +
                      "    a.addToEnd(\"hello\")\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestTypeMismatchAdd");

        assertEquals(1, result.getExitCode(), "Program should exit with code 1");
        assertTrue(result.getError().contains("Stirlang Runtime Error: Type mismatch: cannot add value of type STRING to array of type INT"),
                "Error output was: " + result.getError());
    }

    public void testRuntimeTypeMismatchOnIndexAssign() throws Exception {
        String code = "begin function setElem(arr)\n" +
                      "    arr[0] = \"hello\"\n" +
                      "end function\n\n" +
                      "begin function main()\n" +
                      "    a = {}\n" +
                      "    a.addToEnd(1)\n" +
                      "    setElem(a)\n" +
                      "end function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestTypeMismatchAssign");

        assertEquals(1, result.getExitCode(), "Program should exit with code 1");
        assertTrue(result.getError().contains("Stirlang Runtime Error: Type mismatch: cannot assign value of type STRING to array of type INT"),
                "Error output was: " + result.getError());
    }
}
