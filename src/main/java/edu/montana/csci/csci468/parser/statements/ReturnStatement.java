package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.eval.ReturnException;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

public class ReturnStatement extends Statement {
    private Expression expression;
    private FunctionDefinitionStatement function;

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public void setFunctionDefinition(FunctionDefinitionStatement func) {
        this.function = func;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        if (expression != null) {
            expression.validate(symbolTable);
            if (!function.getType().isAssignableFrom(expression.getType())) {
                expression.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        } else {
            if (!function.getType().equals(CatscriptType.VOID)) {
                addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        }
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        Object returnValue = expression.evaluate(runtime);
        throw new ReturnException(returnValue);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        expression.compile(code);
        boolean isTypeInt = function.getType().equals(CatscriptType.INT);
        boolean isTypeBoolean = function.getType().equals(CatscriptType.BOOLEAN);
        boolean intOrBoolType = isTypeInt || isTypeBoolean;
        boolean isVoid = function.getType().equals(CatscriptType.VOID);
        boolean isObject = function.getType().equals(CatscriptType.OBJECT);

        if(isObject) {
            box(code, expression.getType());
        }

        if(intOrBoolType) {
            code.addInstruction(Opcodes.IRETURN);
        } else if(!isVoid) {
            code.addInstruction(Opcodes.ARETURN);
        } else {
            code.addInstruction(Opcodes.RETURN);
        }

    }

}