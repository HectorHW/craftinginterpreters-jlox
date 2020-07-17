package craftinginterpreters.lox;

import java.util.List;
import java.util.Set;

public interface LoxCallable {
    Set<Integer>  arity();

    default Object call(Interpreter interpreter, List<Object> arguments){
        return call(interpreter, arguments, new Token(TokenType.LEFT_PAREN, "(", null, -1));
    }
    default Object call(Interpreter interpreter, List<Object> arguments, Token paren){
        try{
            return call(interpreter, arguments);
        }catch (RuntimeError e){
            if(e.token.line==-1){
                throw new RuntimeError(new Token(e.token.type,e.token.lexeme,e.token.literal,paren.line),e.getMessage());
            }else{
                throw e;
            }

        }

    }
}
