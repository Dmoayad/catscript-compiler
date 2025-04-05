package edu.montana.csci.csci468.partnertests;
import edu.montana.csci.csci468.CatscriptTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests for Philip Ghede

public class PartnerTests extends CatscriptTestBase{

    @Test
    public void functionForLoopTest() {
        assertEquals("2\n3\n4\n5\n6\n", executeProgram("var a : int = 1\n" +
                "function func(var1 : int) {\n" +
                "for(x in [1,2,3,4,5]){\n" +
                "print(var1+x)}\n" +
                "}\n"+
                "func(1)"));

        assertEquals("8\n9\n10\n11\n12\n", executeProgram("var a : int = 1\n" +
                "function func(var1 : int) {\n" +
                "for(x in [1,2,3,4,5]){\n" +
                "print(var1+x+2)}\n" +
                "}\n"+
                "func(5)"));
    }

    @Test
    public void ifStatementAndPrintingTest() {
        assertEquals("11\n", executeProgram("var x = 1\n" +
                "if(x != 2){\n" +
                "print(x+\"1\")\n" +
                "}\n"));

        assertEquals("x is smaller than 2\n", executeProgram("var x = 1\n" +
                "if(x > 2){\n" +
                "print(\" x variable is larger than 2\")\n" +
                "} else {\n" +
                "print(\"x is smaller than 2\")" +
                "}"));
    }

    @Test
    public void arithmeticWithParenthesisTest() {
        assertEquals("12\n", executeProgram("2*(3+3)"));
        assertEquals("6\n", executeProgram("(2*(3+3))/2"));
        assertEquals("7\n", executeProgram("(2*2)+3"));
        // negative values
        assertEquals("-100\n", executeProgram("100*-1"));
    }

}
