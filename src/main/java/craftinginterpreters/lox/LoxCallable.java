package craftinginterpreters.lox;

import java.util.List;

public interface LoxCallable {
    int arity();

    default Object call(Interpreter interpreter, List<Object> arguments){
        return call(interpreter, arguments, new Token(TokenType.LEFT_PAREN, "(", null, -1));
    }
    default Object call(Interpreter interpreter, List<Object> arguments, Token paren){
        return call(interpreter, arguments);
    }
}
