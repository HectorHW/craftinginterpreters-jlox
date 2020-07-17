package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

public abstract class NativeLoxInstance extends LoxInstance {

    public NativeLoxInstance(LoxClass loxClass) {
        super(loxClass);
    }

    public Object get(Token name){
        if(fields.containsKey(name.lexeme)) return fields.get(name.lexeme);
        var method = loxClass.findMethod(name.lexeme);
        if(method!=null) return method;
        throw new RuntimeError(name, "Undefined property '"+name.lexeme+"'.");
    }
    public void set(Token name, Object value){
        throw new RuntimeError(name,
            "cannot set field in native object");
    }
}
