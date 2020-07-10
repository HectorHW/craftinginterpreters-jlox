package craftinginterpreters.lox;
//перечисление лексем
public enum TokenType {
    //односимвольные
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    QUESTION, COLON,

    //одно- или двусимвольные
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    //литералы
    IDENTIFIER, STRING, NUMBER,

    //зарезервированные ключевые слова
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF,
    //для полноценной работы добавим способ чтения пользовательского ввода
    READNUM, READLINE

}
