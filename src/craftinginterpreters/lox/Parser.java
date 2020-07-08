package craftinginterpreters.lox;

import java.util.List;

import static craftinginterpreters.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Expr parse(){
        try{
            return expression();
        }catch(ParseError error){
            return null;
        }
    }

    private Expr expression(){
        //return equality();
        //return comma_operator();
        //return ternary_operator();
        return comma_operator();
    }

    private Expr comma_operator(){
        Expr expr = ternary_operator();
        while(match(COMMA)){
            Token operator = previous();
            Expr right = ternary_operator();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr ternary_operator() {
        //тернарный оператор ?:
        Expr condition = equality();
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
        return primary();
    }

    private Expr primary(){
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
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
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {return;}
            }
            advance();
        }
    }


}
