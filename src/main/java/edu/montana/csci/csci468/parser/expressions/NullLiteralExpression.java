package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import org.objectweb.asm.Opcodes;

public class NullLiteralExpression extends Expression {

    @Override
    public CatscriptType getType() {
        return CatscriptType.NULL;
    }

    @Override
    public void validate(SymbolTable symbolTable) {}

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        // null literal.
        return null;
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
//        super.compile(code);
        code.pushConstantOntoStack(getType().toString());
    }


}
