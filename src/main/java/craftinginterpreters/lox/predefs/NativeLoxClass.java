package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.LoxCallable;
import craftinginterpreters.lox.LoxClass;
import craftinginterpreters.lox.RuntimeError;
import craftinginterpreters.lox.Token;

import java.util.Map;

public abstract class NativeLoxClass extends LoxClass {

    public NativeLoxClass(String name, LoxClass superclass, Map<String, LoxCallable> methods) {
        super(name, superclass, methods);
    }
    @Override
    public Object get(Token name){
        if(fields.containsKey(name.lexeme)) return fields.get(name.lexeme);
        var method = loxClass.findMethod(name.lexeme);
        if(method!=null) return method;
        throw new RuntimeError(name, "Undefined property '"+name.lexeme+"'.");
    }

    @Override
    public void set(Token name, Object value){
        throw new RuntimeError(name,
            "cannot set field in native object");
    }
}
