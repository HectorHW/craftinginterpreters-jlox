package craftinginterpreters.lox;

import java.util.List;

public interface LoxMacro {
    Object call(Interpreter interpreter);
}
