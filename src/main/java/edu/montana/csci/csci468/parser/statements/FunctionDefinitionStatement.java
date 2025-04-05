package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.eval.ReturnException;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.TypeLiteral;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class FunctionDefinitionStatement extends Statement {
    private String name;
    private CatscriptType type;
    private List<CatscriptType> argumentTypes = new ArrayList<>();
    private List<String> argumentNames = new ArrayList<>();
    private LinkedList<Statement> body;

    public void setName(String name) {
        this.name = name;
    }

    public CatscriptType getType() {
        return type;
    }

    public void setType(TypeLiteral typeLiteral) {
        if (typeLiteral == null) {
            type = CatscriptType.VOID;
        } else {
            addChild(typeLiteral);
            type = typeLiteral.getType();
        }
    }

    public String getName() {
        return name;
    }

    public void addParameter(String name, TypeLiteral typeLiteral) {
        argumentNames.add(name);
        if (typeLiteral == null) {
            argumentTypes.add(CatscriptType.OBJECT);
        } else {
            addChild(typeLiteral);
            argumentTypes.add(typeLiteral.getType());
        }
    }

    public String getParameterName(int i) {
        return argumentNames.get(i);
    }

    public CatscriptType getParameterType(int i) {
        return argumentTypes.get(i);
    }

    public int getParameterCount() {
        return argumentNames.size();
    }

    public void setBody(List<Statement> statements) {
        this.body = new LinkedList<>();
        for (Statement statement : statements) {
            this.body.add(addChild(statement));
        }
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        symbolTable.pushScope();
        for (int i = 0; i < getParameterCount(); i++) {
            if (symbolTable.hasSymbol(getParameterName(i))) {
                addError(ErrorType.DUPLICATE_NAME);
            } else {
                symbolTable.registerSymbol(getParameterName(i), getParameterType(i));
            }
        }
        for (Statement statement : body) {
            statement.validate(symbolTable);
        }
        symbolTable.popScope();
        if (!type.equals(CatscriptType.VOID)) {
            if (!validateReturnCoverage(body)) {
                addError(ErrorType.MISSING_RETURN_STATEMENT);
            }
        }
    }

    private boolean validateReturnCoverage(List<Statement> statements) {
        // size of list statements
        if (statements.get(statements.size() - 1) instanceof ReturnStatement) {
            return true;
        }
        // cycling through the statements and checking instances of if and else.
        for (Statement statement : statements) {
            if (statement instanceof IfStatement) {
                boolean ifCoverage = false, elseCoverage = false;
                // if 'if-statements' that are true list is not empty.
                if (!((IfStatement) statement).getTrueStatements().isEmpty()) {
                    ifCoverage = validateReturnCoverage(((IfStatement) statement).getTrueStatements());
                }
                // if 'if-statements' list that have else statements is not empty
                if (!((IfStatement) statement).getElseStatements().isEmpty()) {
                    elseCoverage = validateReturnCoverage(((IfStatement) statement).getElseStatements());
                }
                // return both of them.
                return ifCoverage && elseCoverage;
            }
        }
        return false;
    }

    public Object invoke(CatscriptRuntime runtime, List<Object> args) {
        runtime.pushScope();
        int parameterCount = getParameterCount();
        for (int i = 0; i < parameterCount; i++) {
            runtime.setValue(getParameterName(i), args.get(i));
        }
        Object returnVal = null;
        try {
            for (Statement statement : body) {
                statement.execute(runtime);
            }
        } catch (ReturnException re) {
            returnVal = re.getValue();
        } finally {
            runtime.popScope();
        }
        return returnVal;
    }

    public String getDescriptor() {
        StringBuilder sb = new StringBuilder("(");
        for (CatscriptType argumentType : argumentTypes) {
            if (argumentType.equals(CatscriptType.BOOLEAN) || argumentType.equals(CatscriptType.INT)) {
                sb.append("I");
            } else {
                sb.append("L").append(internalNameFor(getType().getJavaType())).append(";");
            }
        }
        sb.append(")");
        if (type.equals(CatscriptType.VOID)) {
            sb.append("V");
        } else if (type.equals(CatscriptType.BOOLEAN) || type.equals(CatscriptType.INT)) {
            sb.append("I");
        } else {
            sb.append("L").append(internalNameFor(getType().getJavaType())).append(";");
        }
        return sb.toString();
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        // Function definition do not execute
        // From the explanation Carson gave, we can not remove this we keep it and return nothing
        // So it causes the "side effect" which assign the function and allow execution on function call
        // super.execute(runtime);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
//        super.compile(code);
        code.pushMethod(Opcodes.ACC_PUBLIC, getName(), getDescriptor());

        boolean isTypeInt = getType().equals(CatscriptType.INT);
        boolean isTypeBoolean = getType().equals(CatscriptType.BOOLEAN);
        boolean isObject = getType().equals(CatscriptType.OBJECT);
        boolean isVoid = getType().equals(CatscriptType.VOID);
        boolean intOrBoolType = isTypeInt || isTypeBoolean;

        for(String argumentName : argumentNames){
            Integer argumentSlot = code.createLocalStorageSlotFor(argumentName);
            CatscriptType argumentType = argumentTypes.get(argumentName.indexOf(argumentName));
            if(argumentType.equals(isObject)){
                code.addVarInstruction(Opcodes.ASTORE, argumentSlot);
            }
        }

        for(Statement bodyStatement : body) {
            bodyStatement.compile(code);
        }


        if(intOrBoolType) {
            code.addInstruction(Opcodes.IRETURN);
        } else if(!isVoid) {
            code.addInstruction(Opcodes.ARETURN);
        } else {
            code.addInstruction(Opcodes.RETURN);
        }

        code.popMethod();
    }
}
