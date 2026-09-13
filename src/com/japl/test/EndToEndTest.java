package com.japl.test;

import com.japl.compiler.JAPLCompiler;
import com.japl.runner.ProgramRunner;

import static com.japl.test.JAPLTestRunner.*;

public class EndToEndTest {

    public void testHelloWorld() throws Exception {
        String code = "Begin Function main()\n" +
                      "    print(\"Hello World\")\n" +
                      "End Function";

        JAPLCompiler compiler = new JAPLCompiler();
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

        JAPLCompiler compiler = new JAPLCompiler();
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

        JAPLCompiler compiler = new JAPLCompiler();
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

        JAPLCompiler compiler = new JAPLCompiler();
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

        JAPLCompiler compiler = new JAPLCompiler();
        ProgramRunner.ExecutionResult result = compiler.runSourceAndCapture(code, "TestFunctionCall");

        assertEquals(0, result.getExitCode(), "Program should exit with 0");
        assertTrue(result.getOutput().trim().equals("Hello Alice"),
                "Output was: '" + result.getOutput() + "'");
    }
}
