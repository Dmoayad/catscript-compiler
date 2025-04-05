package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class EqualityExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public EqualityExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
        this.leftHandSide = addChild(leftHandSide);
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isEqual() {
        return operator.getType().equals(TokenType.EQUAL_EQUAL);
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
    }

    @Override
    public CatscriptType getType() {
        return CatscriptType.BOOLEAN;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Object lhsValue = leftHandSide.evaluate(runtime);
        Object rhsValue = rightHandSide.evaluate(runtime);
        // check equal or not.
        if (operator.getType().equals(TokenType.EQUAL_EQUAL)) {
            return lhsValue == rhsValue;
        } else {
            return lhsValue != rhsValue;
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
//        super.compile(code);

        Label setAsFalse = new Label();
        Label setAsEnd = new Label();

        leftHandSide.compile(code);
        box(code, leftHandSide.getType());
        rightHandSide.compile(code);
        box(code, rightHandSide.getType());

        if(isEqual()){
            code.addJumpInstruction(Opcodes.IF_ACMPNE, setAsFalse);
        } else {
            code.addJumpInstruction(Opcodes.IF_ACMPEQ, setAsFalse);
        }

        code.addInstruction(Opcodes.ICONST_1);
        code.addJumpInstruction(Opcodes.GOTO, setAsEnd);
        code.addLabel(setAsFalse);
        code.addInstruction(Opcodes.ICONST_0);
        code.addLabel(setAsEnd);


    }


}
