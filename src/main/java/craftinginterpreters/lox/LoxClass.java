package craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable{
    static final LoxClass anyClass = new LoxClass("any", new HashMap<>());
    final String name;
    private final Map<String, LoxFunction> methods;
    LoxClass(String name, Map<String, LoxFunction> methods){
        super(anyClass);
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString(){
        return name + " class";
    }

    @Override
    public int arity() {
        var initializer = findMethod("init");
        if(initializer==null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);

        var initializer = findMethod("init");
        if(initializer!=null){
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    LoxFunction findMethod(String name){
        if(methods.containsKey(name)) return methods.get(name);
        return null;
    }

    @Override
    Object get(Token name){
        if(methods.containsKey(name.lexeme)) return methods.get(name.lexeme);
        throw new RuntimeError(name, "Undefined property '"+name.lexeme+"'.");
    }
}
