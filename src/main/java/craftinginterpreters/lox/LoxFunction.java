package craftinginterpreters.lox;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;
    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer){
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    LoxFunction bind(LoxInstance instance){
        var environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public Set<Integer> arity() {
        return Collections.singleton(declaration.params.size());
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for(int i=0;i<declaration.params.size();i++){
            environment.define(declaration.params.get(i).lexeme,
                arguments.get(i));
        }
        try{
            interpreter.executeBlock(declaration.body, environment);
        }catch (Interpreter.Return returnValue){
            if(isInitializer) return closure.getAt(0,"this");
            return returnValue.value;
        }
        if(isInitializer) return closure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString(){
        if(declaration.name!=null){
            return "<fn "+declaration.name.lexeme+">";
        }else{
            return "<anon function>";
        }

    }
}
