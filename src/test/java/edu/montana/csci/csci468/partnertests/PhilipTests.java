package edu.montana.csci.csci468.partnertests;

import edu.montana.csci.csci468.CatscriptTestBase;
import org.junit.jupiter.api.Test;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// tests FROM Philip
public class PhilipTests extends CatscriptTestBase {

    //Remame vars/funct as needed
    @Test
    public void tokenTest(){
        assertTokensAre("1+1-12", INTEGER, PLUS, INTEGER, MINUS, INTEGER, EOF);
        assertTokensAre("{{})", LEFT_BRACE, LEFT_BRACE, RIGHT_BRACE, RIGHT_PAREN, EOF);
    }

    @Test
    public void bedmasTest() {
        assertEquals("36\n", executeProgram("(6*(4+2))"));
        assertEquals("9\n", executeProgram("(3*(3+3))/2"));
        assertEquals("-81\n", executeProgram("9*-9"));
    }

    @Test
    public void evalTest(){
        assertEquals(true, evaluateExpression("2 == 2"));
        assertEquals(false, evaluateExpression("2 > 4"));
        assertEquals(true, evaluateExpression("-4 < 2"));
        assertEquals(true, evaluateExpression("true == true"));
    }

}
