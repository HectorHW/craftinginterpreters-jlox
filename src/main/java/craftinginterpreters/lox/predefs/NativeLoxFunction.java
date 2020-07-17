package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.Interpreter;
import craftinginterpreters.lox.LoxCallable;

import java.util.List;
import java.util.Set;

public abstract class NativeLoxFunction implements LoxCallable {

    @Override
    abstract public Set<Integer> arity();

    abstract public Object call(Interpreter interpreter, List<Object> arguments);

    @Override
    public String toString(){
        return "<native fn>";
    }
}
