package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.slf4j.impl.StaticMarkerBinder;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch (RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement stmt = parseStatement();
        if (stmt != null) {
            return stmt;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseStatement(){
        try {
            Statement stmt;
            if(tokens.match(PRINT)) {
                stmt = parsePrintStatement();
                if (stmt != null) {
                    return stmt;
                }
            } if(tokens.match(FUNCTION)) {
                stmt = parseFunctionDeinition();
                if (stmt != null) {
                    return stmt;
                }
            } if(tokens.match(IF)) {
                stmt = parseIfStatement();
                if (stmt != null) {
                    return stmt;
                }
            } if(tokens.match(FOR)) {
                stmt = parseForStatement();
                if (stmt != null) {
                    return stmt;
                }
            } if(tokens.match(VAR)) {
                stmt = parseVariableStatement();
                if (stmt != null) {
                    return stmt;
                }
            } if(tokens.match(IDENTIFIER)) {
                stmt = parseAssignmentStatementOrFunctionCall();
                if (stmt != null) {
                    return stmt;
                }
            } if(currentFunctionDefinition != null){
                stmt = parseReturnStatement();
                if(stmt != null){
                    return stmt;
                }
            }
            return new SyntaxErrorStatement(tokens.consumeToken());
        } catch (UnknownError e) {
            SyntaxErrorStatement syntaxErrorStatement = new SyntaxErrorStatement(tokens.consumeToken());
            while(tokens.hasMoreTokens()){
                if (tokens.match(VAR, FOR, IF, ELSE, PRINT)){
                    break;
                } else {
                    tokens.consumeToken();
                }
            }
            return syntaxErrorStatement;
        }
    }

    private Statement parseAssignmentStatementOrFunctionCall() {
        if (tokens.match(IDENTIFIER)){
            Token id = tokens.getCurrentToken();
            tokens.consumeToken();
            if (tokens.matchAndConsume(EQUAL)){
                return parseAssignmentStatement(id);
            }
            return new FunctionCallStatement(parseFunctionCall(id));
        }
        return null;
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    private FunctionDefinitionStatement parseFunctionDeinition(){
        if(tokens.match(FUNCTION)){
            FunctionDefinitionStatement func = new FunctionDefinitionStatement();
            func.setStart(tokens.consumeToken());
            Token functionName = require(IDENTIFIER, func);
            func.setName(functionName.getStringValue());
            require(LEFT_PAREN, func);
            if(!tokens.match(RIGHT_PAREN)){
                do {
                Token paramName = require(IDENTIFIER, func);
                TypeLiteral typeLiteral = null;
                if(tokens.matchAndConsume(COLON)){
                    typeLiteral = parseTypeLiteral();
                }
                func.addParameter(paramName.getStringValue(), typeLiteral);

                } while (tokens.matchAndConsume(COMMA));
            }
            require(RIGHT_PAREN, func);
            TypeLiteral typeLiteral = null;
            if(tokens.matchAndConsume(COLON)){
                typeLiteral = parseTypeLiteral();
            }
            func.setType(typeLiteral);
            currentFunctionDefinition = func;

            require(LEFT_BRACE, func);
            LinkedList<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()){
                statements.add(parseStatement());
            }
            require(RIGHT_BRACE, func);
            func.setBody(statements);
            return func;
        } else {
            return null;
        }
    }

    private AssignmentStatement parseAssignmentStatement(Token identifier) {
        AssignmentStatement assignmentStatement = new AssignmentStatement();
        assignmentStatement.setStart(identifier);
        assignmentStatement.setVariableName(identifier.getStringValue());
        assignmentStatement.setExpression(parseExpression());
        assignmentStatement.setEnd(tokens.lastToken());
        return assignmentStatement;
    }

    private IfStatement parseIfStatement(){
        if(tokens.match(IF)){
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, ifStatement);
            ifStatement.setExpression(parseExpression());
            require(RIGHT_PAREN, ifStatement);
            require(LEFT_BRACE, ifStatement);
            List<Statement> stmtList = new ArrayList<>();
            while(!tokens.match(ELSE) && !tokens.match(EOF) && !tokens.match(RIGHT_BRACE)){
                stmtList.add(parseProgramStatement());
            }
            ifStatement.setTrueStatements(stmtList);
            ifStatement.setEnd(require(RIGHT_BRACE, ifStatement));

            if(tokens.match(ELSE)){
                tokens.consumeToken();
                List<Statement> elseStatements = new ArrayList<>();
                if (tokens.match(IF)){
                    elseStatements.add(parseIfStatement());
                } else {
                    require(LEFT_BRACE, ifStatement);
                    while (!tokens.match(EOF) && !tokens.match(RIGHT_BRACE)){
                        elseStatements.add(parseProgramStatement());
                    }
                    require(RIGHT_BRACE, ifStatement);
                }
                ifStatement.setElseStatements(elseStatements);
            }
            return ifStatement;
        }
        return null;
    }

    private VariableStatement parseVariableStatement(){
        if (tokens.match(VAR)){
            VariableStatement variableStatement = new VariableStatement();
            variableStatement.setStart(tokens.consumeToken());
            variableStatement.setVariableName(require(IDENTIFIER, variableStatement).getStringValue());
            if (tokens.matchAndConsume(COLON)){
                CatscriptType type;
                type = parseExpressionType();
                variableStatement.setExplicitType(type);
            }
            require(EQUAL, variableStatement);
            variableStatement.setExpression(parseExpression());
            variableStatement.setEnd(tokens.lastToken());
            return variableStatement;
        }
        return null;
    }

    private ForStatement parseForStatement(){
        if(tokens.match(FOR)){
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);

            forStatement.setVariableName(require(IDENTIFIER, forStatement).getStringValue());
            require(IN, forStatement);
            forStatement.setExpression(parseExpression());
            require(RIGHT_PAREN, forStatement);
            require(LEFT_BRACE, forStatement);
            List<Statement> stmtList = new ArrayList<>();

            while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)){
                stmtList.add(parseStatement());
            }
            forStatement.setBody(stmtList);
            forStatement.setEnd(require(RIGHT_BRACE, forStatement));

            return forStatement;
        }
        return null;
    }

    private TypeLiteral parseTypeLiteral() {
        if (tokens.match("int")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.INT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if (tokens.match("string")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.STRING);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if (tokens.match("bool")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.BOOLEAN);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if (tokens.match("object")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.OBJECT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if (tokens.match("list")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
            typeLiteral.setToken(tokens.consumeToken());
            if(tokens.matchAndConsume(LESS)){
                TypeLiteral componentType = parseTypeLiteral();
                typeLiteral.setType(CatscriptType.getListType(componentType.getType()));
                require(GREATER, typeLiteral);
            }
            return typeLiteral;
        }
        TypeLiteral typeLiteral = new TypeLiteral();
        typeLiteral.setType(CatscriptType.OBJECT);
        typeLiteral.setToken(tokens.consumeToken());
        typeLiteral.addError(ErrorType.BAD_TYPE_NAME);
        return typeLiteral;
    }

    private Statement parseReturnStatement() {
        ReturnStatement returnStatement = new ReturnStatement();
        returnStatement.setStart(tokens.consumeToken());
        returnStatement.setFunctionDefinition(currentFunctionDefinition);
        if (!tokens.match(EOF) && !tokens.match(RIGHT_BRACE)) {
            returnStatement.setExpression(parseExpression());
        }
        return returnStatement;
    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private Expression parseEqualityExpression() {
        Expression expression = parseComparisonExpression();
        while (tokens.match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private CatscriptType parseExpressionType(){
        CatscriptType type;
        String typeLiteral = tokens.getCurrentToken().getStringValue();
        tokens.consumeToken();
        if(typeLiteral.equals("int")){
            type = CatscriptType.INT;
            return type;
        } if(typeLiteral.equals("string")){
            type = CatscriptType.STRING;
            return type;
        } if(typeLiteral.equals("bool")){
            type = CatscriptType.BOOLEAN;
            return type;
        } if (typeLiteral.equals("object")){
            type = CatscriptType.OBJECT;
            return type;
        } if(typeLiteral.equals("list")){
            if(tokens.match(LESS)){
                tokens.consumeToken();
                type = CatscriptType.getListType(parseExpressionType());
                tokens.consumeToken();
            } else {
                // Defaulting to Object.
                type = CatscriptType.getListType(CatscriptType.OBJECT);
            }
            return type;
        }
        return null;
    }

    private Expression parseComparisonExpression() {
        Expression expression = parseAdditiveExpression();
        while (tokens.match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;
        }
        return expression;
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }

    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression();
        while (tokens.match(SLASH, STAR)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if (tokens.match(LEFT_BRACKET)) {
            return parseListLiteral();
        } else if (tokens.match(LEFT_PAREN)) {
            return parseParenthesizedExpression();
        } else if (tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringLiteralExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringLiteralExpression.setToken(stringToken);
            return stringLiteralExpression;
        } else if (tokens.match(TRUE)) {
            Token booleanToken = tokens.consumeToken();
            BooleanLiteralExpression booleanExpression = new BooleanLiteralExpression(Boolean.parseBoolean(booleanToken.getStringValue()));
            booleanExpression.setToken(booleanToken);
            return booleanExpression;
        } else if (tokens.match(FALSE)) {
            Token booleanToken = tokens.consumeToken();
            BooleanLiteralExpression booleanExpression = new BooleanLiteralExpression(Boolean.parseBoolean(booleanToken.getStringValue()));
            booleanExpression.setToken(booleanToken);
            return booleanExpression;
        } else if (tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullExpression = new NullLiteralExpression();
            nullExpression.setToken(nullToken);
            return nullExpression;
        } else if (tokens.match(IDENTIFIER)) {
            Token id = tokens.consumeToken();
            if (tokens.match(LEFT_PAREN)) {
                return parseFunctionCall(id);
            } else {
                return parseIdentifierExpression(id);
            }
        } else {
            throw new UnknownExpressionParseError();
//            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
//            syntaxErrorExpression.setToken(tokens.consumeToken());
//            return syntaxErrorExpression;
        }
    }

    class UnknownExpressionParseError extends RuntimeException {

    }

    private IdentifierExpression parseIdentifierExpression(Token id) {
        IdentifierExpression identifierExpression = new IdentifierExpression(id.getStringValue());
        identifierExpression.setToken(id);
        return identifierExpression;
    }

    private FunctionCallExpression parseFunctionCall(Token id) {
        IdentifierExpression identifierExpression = new IdentifierExpression(id.getStringValue());
        List<Expression> args = new ArrayList<>();
        if (tokens.matchAndConsume(LEFT_PAREN)) {
            if(!tokens.match(RIGHT_PAREN)) {
                do {
                    Expression expression = parseExpression();
                    args.add(expression);
                } while (tokens.matchAndConsume(COMMA));
            }
            FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierExpression.getName(),
                    args);
            boolean foundParen = tokens.match(RIGHT_PAREN);
            if (foundParen) {
                Token token = tokens.consumeToken();
                functionCallExpression.setEnd(token);
            } else {
                functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
            }
            return functionCallExpression;
        }
        return null;
    }

    private StringLiteralExpression parseStringExpression() {
        if (tokens.match(STRING)) {
            String string = tokens.consumeToken().getStringValue();
            StringLiteralExpression stringLiteralExpression = new StringLiteralExpression(string);
            return stringLiteralExpression;
        }
        return null;
    }

    private BooleanLiteralExpression parseBoolean() {
        if (tokens.match(TRUE)) {
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(true);
            return booleanLiteralExpression;
        } else if (tokens.match(FALSE)) {
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(false);
            return booleanLiteralExpression;
        }
        return null;
    }

    private NullLiteralExpression parseNull() {
        if (tokens.match(NULL)) {
            Token token = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            return nullLiteralExpression;
        }
        return null;
    }

    private Expression parseParenthesizedExpression() {
//        Expression innerExpression;
        if (tokens.match(LEFT_PAREN)) {
            Token parenStart = tokens.consumeToken();
            Expression innerExpression = parseExpression();
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(innerExpression);
            parenthesizedExpression.setStart(parenStart);
            boolean foundParen = tokens.match(RIGHT_PAREN);
            if (foundParen) {
                Token token = tokens.consumeToken();
                parenthesizedExpression.setEnd(token);
            } else {
                parenthesizedExpression.addError(ErrorType.UNEXPECTED_TOKEN);
            }
            return parenthesizedExpression;
        }
        return null;
    }


    private Expression parseListLiteral() {
        if (tokens.match(LEFT_BRACKET)) {
            Token listStart = tokens.consumeToken();
            List<Expression> exprs = new ArrayList<>();
            if (!tokens.match(RIGHT_BRACKET)) {
                do {
                    Expression expression = parseExpression();
                    exprs.add(expression);
                } while (tokens.matchAndConsume(COMMA));
            }
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(exprs);
            listLiteralExpression.setStart(listStart);
            boolean foundBracket = tokens.match(RIGHT_BRACKET);
            if (foundBracket) {
                Token token = tokens.consumeToken();
                listLiteralExpression.setEnd(token);
            } else {
                listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
            }
            return listLiteralExpression;
        }
        return null;
    }

    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if (tokens.match(type)) {
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
