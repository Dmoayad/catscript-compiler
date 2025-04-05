package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.statements.FunctionCallStatement;
import edu.montana.csci.csci468.parser.statements.FunctionDefinitionStatement;
import org.objectweb.asm.Opcodes;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class IdentifierExpression extends Expression {
    private final String name;
    private CatscriptType type;

    public IdentifierExpression(String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        CatscriptType type = symbolTable.getSymbolType(getName());
        if (type == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            this.type = type;
        }
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        // return the name of the identifier.
        return runtime.getValue(name);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
//        super.compile(code);

        Integer identifierStorage = code.resolveLocalStorageSlotFor(getName());
        boolean isTypeInt = getType().equals(CatscriptType.INT);
        boolean isTypeBoolean = getType().equals(CatscriptType.BOOLEAN);
        boolean intOrBoolType = isTypeInt || isTypeBoolean;
        String descriptorIdentifier;
        if (identifierStorage != null) {
            if (intOrBoolType) {
                code.addVarInstruction(Opcodes.ILOAD, identifierStorage);
            } else {
                code.addVarInstruction(Opcodes.ALOAD, identifierStorage);
            }
        } else {
            code.addVarInstruction(Opcodes.ALOAD, 0);
            if (intOrBoolType) {
                descriptorIdentifier = "I";
            } else {
                descriptorIdentifier = "L" + internalNameFor(getType().getJavaType()) + ";";
            }
            code.addFieldInstruction(Opcodes.GETFIELD, name, descriptorIdentifier,
                    code.getProgramInternalName());
        }


    }
}
