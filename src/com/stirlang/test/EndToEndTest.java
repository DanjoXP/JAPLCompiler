package com.stirlang.test;

import com.stirlang.compiler.StirlangCompiler;
import com.stirlang.runner.ProgramRunner;

import static com.stirlang.test.StirlangTestRunner.*;

public class EndToEndTest {

    public void testHelloWorld() throws Exception {
        String code = "Begin Function main()\n" +
                      "    print(\"Hello World\")\n" +
                      "End Function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestHello");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Hello World"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testAdultCheck() throws Exception {
        String code = "Begin Function main()\n" +
                      "    age = 30\n" +
                      "    if age < 18\n" +
                      "        print(\"Hello World\")\n" +
                      "    else\n" +
                      "        print(\"You are an adult\")\n" +
                      "    end if\n" +
                      "End Function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestAdultCheck");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("You are an adult"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testMathPrecedenceAndCompoundAssign() throws Exception {
        String code = "Begin Function main()\n" +
                      "    result = 10 + 5 * 2\n" +
                      "    result += 10\n" +
                      "    print(result)\n" +
                      "End Function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestMath");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("30"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testReadableComparisonsAndBooleans() throws Exception {
        String code = "Begin Function main()\n" +
                      "    age = 20\n" +
                      "    active = true\n" +
                      "    if age greaterThan 18 and active equalTo true\n" +
                      "        print(\"Eligible\")\n" +
                      "    end if\n" +
                      "End Function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestReadableComparisons");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Eligible"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testFunctionCall() throws Exception {
        String code = "Begin Function greet(name)\n" +
                      "    print(\"Hello \" + name)\n" +
                      "End Function\n\n" +
                      "Begin Function main()\n" +
                      "    greet(\"Alice\")\n" +
                      "End Function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestFunctionCall");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Hello Alice"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testTopLevelFunctionCallWithoutMain() throws Exception {
        String code = "Begin Function GreetPerson(name)\n" +
                      "    print(\"Hello, \" + name)\n" +
                      "End Function\n\n" +
                      "GreetPerson(\"Danny\")";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestGreetDanny");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Hello, Danny"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testCountedLoopWithExposedCounter() throws Exception {
        String code = "Begin Function main()\n" +
                      "    Start Loop(5) as i\n" +
                      "        print(i)\n" +
                      "    End Loop\n" +
                      "End Function";

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
        String code = "Begin Function main()\n" +
                      "    total = 0\n" +
                      "    Start Loop(4)\n" +
                      "        total += 2\n" +
                      "    End Loop\n" +
                      "    print(total)\n" +
                      "End Function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestLoopUnexposed");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("8"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testNestedLoops() throws Exception {
        String code = "Begin Function main()\n" +
                      "    Start Loop(2) as outer\n" +
                      "        Start Loop(3) as inner\n" +
                      "            print(outer + \":\" + inner)\n" +
                      "        End Loop\n" +
                      "    End Loop\n" +
                      "End Function";

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
        String code = "Begin Function main()\n" +
                      "    Start Loop(10) as i\n" +
                      "        if i == 2\n" +
                      "            Continue Loop\n" +
                      "        end if\n" +
                      "        if i == 5\n" +
                      "            Break Loop\n" +
                      "        end if\n" +
                      "        print(i)\n" +
                      "    End Loop\n" +
                      "End Function";

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
        String code = "Begin Function main()\n" +
                      "    n = 0\n" +
                      "    Start Loop\n" +
                      "        n += 1\n" +
                      "        if n == 3\n" +
                      "            Break Loop\n" +
                      "        end if\n" +
                      "    End Loop\n" +
                      "    print(n)\n" +
                      "End Function";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestInfiniteBreak");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("3"),
                "Output was: '" + result.getOutput() + "'");
    }

    public void testTopLevelLoop() throws Exception {
        String code = "total = 0\n" +
                      "Start Loop(5) as idx\n" +
                      "    total += idx\n" +
                      "End Loop\n" +
                      "print(total)";

        StirlangCompiler compiler = new StirlangCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestTopLevelLoop");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("10"),
                "Output was: '" + result.getOutput() + "'");
    }
}
