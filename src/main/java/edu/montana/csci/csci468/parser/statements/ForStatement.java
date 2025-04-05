package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

import org.objectweb.asm.Label;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ForStatement extends Statement {
    private Expression expression;
    private String variableName;
    private List<Statement> body;

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setBody(List<Statement> statements) {
        this.body = new LinkedList<>();
        for (Statement statement : statements) {
            this.body.add(addChild(statement));
        }
    }

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        symbolTable.pushScope();
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME);
        } else {
            expression.validate(symbolTable);
            CatscriptType type = expression.getType();
            if (type instanceof CatscriptType.ListType) {
                symbolTable.registerSymbol(variableName, getComponentType());
            } else {
                addError(ErrorType.INCOMPATIBLE_TYPES, getStart());
                symbolTable.registerSymbol(variableName, CatscriptType.OBJECT);
            }
        }
        for (Statement statement : body) {
            statement.validate(symbolTable);
        }
        symbolTable.popScope();
    }

    private CatscriptType getComponentType() {
        return ((CatscriptType.ListType) expression.getType()).getComponentType();
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        // loop through and execute the body statements.
        for (Object var : (List<Object>) expression.evaluate(runtime)) {
            runtime.setValue(variableName, var);
            for (Statement bodyStatement : body) {
                bodyStatement.execute(runtime);
            }
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
//        super.compile(code);
        Integer iteratorSlot = code.nextLocalStorageSlot();

        Label iterationStart = new Label();
        Label setAsEnd = new Label();

        expression.compile(code);
        code.addMethodInstruction(Opcodes.INVOKEINTERFACE, ByteCodeGenerator.internalNameFor(List.class),
                "iterator", "()Ljava/util/Iterator;");
        code.addVarInstruction(Opcodes.ASTORE, iteratorSlot);

         code.addLabel(iterationStart);
         code.addVarInstruction(Opcodes.ALOAD, iteratorSlot);
         code.addMethodInstruction(Opcodes.INVOKEINTERFACE, ByteCodeGenerator.internalNameFor(Iterator.class),
                 "hasNext", "()Z");
         code.addJumpInstruction(Opcodes.IFEQ, setAsEnd);

         CatscriptType componentType = getComponentType();
         code.addVarInstruction(Opcodes.ALOAD, iteratorSlot);
         code.addMethodInstruction(Opcodes.INVOKEINTERFACE, ByteCodeGenerator.internalNameFor(Iterator.class),
                 "next", "()Ljava/lang/Object;");

         code.addTypeInstruction(Opcodes.CHECKCAST, ByteCodeGenerator.internalNameFor(componentType.getJavaType()));
         unbox(code, componentType);

         Integer LocalStorageSlotFor = code.createLocalStorageSlotFor(variableName);

         boolean isTypeint = componentType.equals(CatscriptType.INT);
         boolean isTypeBoolean = componentType.equals(CatscriptType.BOOLEAN);
         boolean isIntOrBoolean = isTypeint || isTypeBoolean;

         if(isIntOrBoolean){
             code.addVarInstruction(Opcodes.ISTORE, LocalStorageSlotFor);
         } else {
             code.addVarInstruction(Opcodes.ASTORE, LocalStorageSlotFor);
         }
         for(Statement bodyStatement: body){
             bodyStatement.compile(code);
         }
         code.addJumpInstruction(Opcodes.GOTO, iterationStart);
         code.addLabel(setAsEnd);
    }

}
