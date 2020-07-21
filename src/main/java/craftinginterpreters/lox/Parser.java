package craftinginterpreters.lox;

import craftinginterpreters.lox.predefs.LoxBoolean;

import java.util.ArrayList;
import java.util.List;

import static craftinginterpreters.lox.TokenType.*;

public class Parser {
    static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;
    //private boolean inLoop = false;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    Expr parseAsExpression(){
        return expression();
    }

    private Expr expression(){
        //return equality();
        //return comma_operator();
        //return ternary_operator();
        //return comma_operator();
        return comma_operator();
        //return null;
    }


    private Stmt declaration(){
        try{
            if(check(DEF)&&checkNext(IDENTIFIER)){
                match(DEF);
                return function("function");
            }
            if(match(CLASS)) return classDeclaration();
            if(match(VAR)) return varDeclaration();
            return statement();
        }catch (ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt statement(){
        if(match(PRINT)) return printStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());
        if(match(IF)) return ifStatement();
        if(match(WHILE)) return whileStatement();
        if(match(FOR)) return forStatement();
        if (match(BREAK, CONTINUE)) return loopcontrolStatement();
        if(match(RETURN)) return returnStatement();
        return expressionStatement();
    }

    private Stmt classDeclaration(){
        Token name = consume(IDENTIFIER, "Expected class name");

        Expr.Variable superclass = null;
        if(match(LESS)){
            consume(IDENTIFIER, "Expected superclass name");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expected `{` before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()){
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expected `}` after class body.");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt printStatement(){
        Expr value = expression();
        consume(SEMICOLON, "Expected `;` after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement(){
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON)){
            value = expression();
        }

        consume(SEMICOLON, "Expected `;` after return value");
        return new Stmt.Return(keyword, value);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expected `;` after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind){ //этот же способ можно использовать для методов
        boolean isStatic = false;
        if(match(STATIC)) isStatic = true;
        Token name = consume(IDENTIFIER, "Expected "+kind+" name.");
        consume(LEFT_PAREN, "Expected `(` after "+kind+" name.");
        List<Token> parameters = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do{
                if(parameters.size()>=255){
                    error(peek(), "Cannot have more than 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Expected parameter name."));
            }while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect `)` after parameters.");

        consume(LEFT_BRACE, "Expected `{` before "+kind+" body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body, isStatic);

    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expected `(` after if.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected `)` after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement(){
        Expr condition;
        if(match(LEFT_PAREN)){
            //consume(LEFT_PAREN, "Expected `(` after while.");
            condition = expression();
            consume(RIGHT_PAREN, "Expected `)` after while condition.");
        }else{
            condition = new Expr.Literal(LoxBoolean.TRUE);
        }

        //inLoop = true;
        Stmt body = statement();
        //inLoop = false;
        return new Stmt.While(condition, body);
    }

    private Stmt forStatement(){ //в нашем случае for - немного другой вид цикла
        consume(LEFT_PAREN, "Expected `(` after for.");
        Stmt initializer;
        if(match(SEMICOLON)){ //for (;
            initializer = null;
        }else if(match(VAR)){ // for(var ...;
            initializer = varDeclaration();
        }else{ //for(<expr>;
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(SEMICOLON)){ //for(..;<cond>;
            condition = expression();
        }else{
            condition = new Expr.Literal(LoxBoolean.TRUE);
            //else for(..;;
        }
        consume(SEMICOLON, "Expected `;` after loop condition.");

        Expr increment = null;
        if(!check(RIGHT_PAREN)){
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected `)` after for clauses");
        //inLoop = true;
        Stmt body = statement();
        //inLoop = false;
        /*
        if(increment!=null){ //добавляем операцию инкремента в конец блока
            body = new Stmt.Block(Arrays.asList(
                body, new Stmt.Expression(increment)
            ));
        }
        if(condition==null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if(initializer!=null){ //добавляем инициализатор если он есть
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return body;
         */
        return new Stmt.For(initializer, condition, body, increment);
    }

    private Stmt loopcontrolStatement(){
        //if(!inLoop) throw error(previous(), "loop control statement outside of loop.");
        var statement = new Stmt.ControlStatement(previous());
        consume(SEMICOLON, "Expected `;` after loop control statement.");
        return statement;
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expected `}` after block.");
        return statements;
    }

    private Expr assignment(){
        Expr expr = ternary_operator();
        if(match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }else if(expr instanceof Expr.Get){
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }else
            if(match(PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                //превращаем a+=... в a = a + ...
                var shorthand = switch (equals.type){
                    case PLUS_EQUAL -> new Expr.Binary(
                        new Expr.Variable(name), new Token(
                        PLUS, "+", null, name.line
                    ), value);
                    case MINUS_EQUAL -> new Expr.Binary(
                        new Expr.Variable(name), new Token(
                        MINUS, "-", null, name.line
                    ), value);
                    case STAR_EQUAL -> new Expr.Binary(
                        new Expr.Variable(name), new Token(
                        STAR, "*", null, name.line
                    ), value);
                    case SLASH_EQUAL -> new Expr.Binary(
                        new Expr.Variable(name), new Token(
                        SLASH, "/", null, name.line
                    ), value);
                    default -> null;
                };
                return new Expr.Assign(name, shorthand);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expected variable name.");
        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression();
        }
        consume(SEMICOLON, "Expected `;` after variable declaration.");
        return new Stmt.Var(name, initializer);
    }


    private Expr comma_operator(){
        Expr expr = assignment();
        while(match(COMMA)){
            Token operator = previous();
            Expr right = assignment();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr ternary_operator() {
        //тернарный оператор ?:
        Expr condition = anonFunction();
        if(match(QUESTION)){ //не думаю, что можно составить схему a+b+c из тернарных операторов, так что if
            Token op1 = previous();
            Expr expr2 = expression();
            Token op2 = consume(COLON, "`:` expected for ternary operator.");
            Expr expr3 = equality();
            condition = new Expr.Ternary(
                condition, op1, expr2, op2, expr3
            );
        }
        return condition;
    }

    private Expr anonFunction(){
        if(match(DEF)){
            consume(LEFT_PAREN, "Expected `(` after anon function definition");
            List<Token> parameters = new ArrayList<>();
            if(!check(RIGHT_PAREN)){
                do{
                    if(parameters.size()>=255){
                        error(peek(), "Cannot have more than 255 parameters.");
                    }
                    parameters.add(consume(IDENTIFIER, "Expected parameter name."));
                }while(match(COMMA));
            }
            consume(RIGHT_PAREN, "Expect `)` after parameters.");

            consume(LEFT_BRACE, "Expected `{` before anon function body.");
            List<Stmt> body = block();
            return new Expr.AnonFun( parameters, body);
        }else{
            return or();
        }
    }


    private Expr or(){
        Expr expr = and();

        while(match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and(){
        Expr expr = equality();
        while(match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator,right);
        }
        return expr;
    }

    private Expr equality(){
        Expr expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)){ //работаем с != или ==
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison(){
        Expr expr = addition();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition(){
        Expr expr = multiplication();
        while(match(MINUS, PLUS)){
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication(){
        Expr expr = unary();

        while(match(SLASH, STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary(){
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call(){
        Expr expr = primary();
        while(true){ // вспомогательный метод для обработки вызовов вида f(...)(...)...
            if(match(LEFT_PAREN)){
                expr = finishCall(expr);
            }else if(match(DOT)) {
                Token name = consume(IDENTIFIER, "Expected property name after `.`.");
                expr = new Expr.Get(expr, name);

            }else{
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr calee){
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do{ // a
                if(arguments.size()>=255){
                    error(peek(), "Cannot have more than 255 arguments.");
                }
                arguments.add(ternary_operator());
            }while(match(COMMA)); //,b,c...
        }
        Token paren = consume(RIGHT_PAREN, "Expect `)` after argument list.");
        return new Expr.Call(calee, paren, arguments);
    }

    private Expr primary(){
        if (match(FALSE)) return new Expr.Literal(LoxBoolean.FALSE);
        if (match(TRUE)) return new Expr.Literal(LoxBoolean.TRUE);
        if (match(NIL)) return new Expr.Literal(null);

        if(match(THIS)) return new Expr.This(previous());
        if(match(SUPER)){
            Token keyword = previous();
            consume(DOT, "Expected `.` after `super`.");
            Token method = consume(IDENTIFIER, "Expected superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if(match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if(match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "expression expected.");
    }


    private boolean match(TokenType... types){
        for(TokenType type : types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message){
        if(check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }
    private boolean checkNext(TokenType type){
        if(current+1+1==this.tokens.size()) return false;
        return tokens.get(current+1).type ==type;

    }

    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize(){
        advance();
        while(!isAtEnd()){
            if (previous().type==SEMICOLON) return;
            switch (peek().type){
                case CLASS, DEF, VAR, FOR, IF, WHILE, PRINT, RETURN -> {return;}
            }
            advance();
        }
    }


}
