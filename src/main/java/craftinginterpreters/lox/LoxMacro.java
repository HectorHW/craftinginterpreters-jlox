package craftinginterpreters.lox;

import java.util.List;

public interface LoxMacro {
    Environment env = null;
    Object call(Interpreter interpreter);
}
