package craftinginterpreters.lox;

import java.util.*;

public class LoxClass extends LoxInstance implements LoxCallable{
    public static final LoxClass anyClass = new LoxClass("any", null, new HashMap<>());
    final String name;
    final LoxClass superclass;
    protected final Map<String, LoxFunction> methods;
    public LoxClass(String name,LoxClass superclass, Map<String, LoxFunction> methods){
        super(anyClass);
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }

    @Override
    public String toString(){
        return name + " class";
    }

    @Override
    public Set<Integer> arity() {
        var initializer = findMethod("init");
        if(initializer==null) return Collections.singleton(0);
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
        if(superclass!=null) return superclass.findMethod(name);

        return null;
    }

    @Override
    Object get(Token name){
        Object method = this.findMethod(name.lexeme);
        if(method!=null) return method;
        throw new RuntimeError(name, "Undefined property '"+name.lexeme+"'.");
    }
}
