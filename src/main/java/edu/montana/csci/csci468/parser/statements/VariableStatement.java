package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

public class VariableStatement extends Statement {
    private Expression expression;
    private String variableName;
    private CatscriptType explicitType;
    private CatscriptType type;

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public void setExplicitType(CatscriptType type) {
        this.explicitType = type;
    }

    public CatscriptType getExplicitType() {
        return explicitType;
    }

    public boolean isGlobal() {
        return getParent() instanceof CatScriptProgram;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME);
        } else {
            // TODO if there is an explicit type, ensure it is correct
            //      if not, infer the type from the right hand side expression
            if (getExplicitType() != null) {
                if (!getExplicitType().isAssignableFrom(expression.getType())) {
                    addError(ErrorType.INCOMPATIBLE_TYPES);
                } else {
                    type = getExplicitType();
                }
            } else {
                type = expression.getType();
            }
            symbolTable.registerSymbol(variableName, type);
        }
    }

    public CatscriptType getType() {
        return type;
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        Object variableValue = expression.evaluate(runtime);
        runtime.setValue(variableName, variableValue);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
//        super.compile(code);
        String descriptorString;
        boolean isTypeInt = getType().equals(CatscriptType.INT);
        boolean isTypeBoolean = getType().equals(CatscriptType.BOOLEAN);
        boolean intOrBoolType = isTypeInt || isTypeBoolean;

        if(isGlobal()) {
            if(intOrBoolType){
                code.addVarInstruction(Opcodes.ALOAD, 0);
                expression.compile(code);
                descriptorString = "I";
            } else {
                code.addVarInstruction(Opcodes.ALOAD, 0);
                expression.compile(code);
                descriptorString = "L" + ByteCodeGenerator.internalNameFor(getType().getJavaType())+";";
            }
            code.addField(variableName, descriptorString);
            code.addFieldInstruction(Opcodes.PUTFIELD, variableName, descriptorString, code.getProgramInternalName());
        } else {
            Integer localStorageSlotFor = code.createLocalStorageSlotFor(variableName);
            expression.compile(code);
            if(intOrBoolType){
                code.addVarInstruction(Opcodes.ISTORE, localStorageSlotFor);
            } else {
                code.addVarInstruction(Opcodes.ASTORE, localStorageSlotFor);
            }
        }




    }
}
