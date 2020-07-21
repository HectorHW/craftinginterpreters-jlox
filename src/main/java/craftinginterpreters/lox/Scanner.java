package craftinginterpreters.lox;

import craftinginterpreters.lox.predefs.LoxNumber;
import craftinginterpreters.lox.predefs.LoxString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static craftinginterpreters.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("def", DEF);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("break", BREAK);
        keywords.put("continue", CONTINUE);
        keywords.put("static", STATIC);
    }


    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){
        while(!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            //case '-' -> addToken(MINUS);
            case '-' -> addToken(match('=') ? MINUS_EQUAL : MINUS);
            //case '+' -> addToken(PLUS);
            case '+' -> addToken(match('=') ? PLUS_EQUAL : PLUS);
            case ';' -> addToken(SEMICOLON);
            //case '*' -> addToken(STAR);
            case '*' -> addToken(match('=') ? STAR_EQUAL : STAR);

            case '?' -> addToken(QUESTION);
            case ':' -> addToken(COLON);

            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG); // != / !
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL); // == / =
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS); // <= / <
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);// >= / >

            case '/' -> { // обработка символа `/` - деление или комментарий
                if(match('/')) {
                while(peek()!='\n' && !isAtEnd()) advance(); //пропустим оставшуюся часть строки
            }else if(match('*')){ // /*
                    while ((peek()!='*' || peekNext()!='/') && !isAtEnd()){ // */
                        if(peek()=='\n') line++;
                        advance();
                    }
                    if(isAtEnd()) {
                        Lox.error(line, "Unterminated multiline comment.");
                        return;
                    }
                    advance(); advance();
                }
                else{
                //addToken(SLASH);
                addToken(match('=') ? SLASH_EQUAL : SLASH);
            } }

            case ' ', '\t', '\r' -> {}
            case '\n' -> line++;

            case '"' -> string();

            default -> {
                if(isDigit(c)){
                    number();
                }else if(isAlpha(c)){
                    identifier();
                }else {
                    Lox.error(line, "Unexpected character.");
                }

            }
        }
    }


    private void identifier(){
        while(isAlphaNumeric(peek())) advance();

        var text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if(type==null) type = IDENTIFIER;

        addToken(type);
    }

    private void number(){
        while (isDigit(peek())) advance();

        //проверим наличие точки в числе
        if(peek()=='.' && isDigit(peekNext())){
            advance(); //пропутим точку
            while (isDigit(peek())) advance(); //и оставшуюся часть числа
        }
        var value = source.substring(start, current);
        addToken(NUMBER, new LoxNumber.LoxNumberInstance(Double.parseDouble(value)));
    }

    private void string(){
        while(peek()!='"' && !isAtEnd()){
            if (peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()){
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance(); // пропускаем закрывающую "
        var value = source.substring(start+1, current-1); //не включая ""
        addToken(STRING, new LoxString.LoxStringInstance(value));
    }

    private boolean match(char expected){ //проверка на совпадение следующего символа при формировании токена
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;
        current++;
        return true;
    }
    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c){
        return (c>='a' && c<='z') ||
            (c>='A' && c<='Z') ||
            c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c){
        return c>='0' && c<='9';
    }

    private boolean isAtEnd(){
        return current>=source.length();
    }

    private char advance(){
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        var text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
