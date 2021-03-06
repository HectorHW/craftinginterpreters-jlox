package craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing; //добавим scope и shadowing
    private final Map<String, Object> values = new HashMap<>();

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    public Object get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        if(enclosing!=null) return enclosing.get(name);
        throw new RuntimeError(name, "Undefined variable `"+name.lexeme+"`.");
    }

    public Object get(String name){
        if(values.containsKey(name)){
            return values.get(name);
        }
        if(enclosing!=null) return enclosing.get(name);
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, name, null, -1), "Undefined variable `"+name+"`.");
    }

    void assign(Token name, Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }
        if(enclosing!=null){
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable `"+name.lexeme+"`.");
    }

    void define(String name, Object value){
        values.put(name, value);
    }

    Object getOrDefault(Token name, Object otherwise){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        if(enclosing!=null) return enclosing.getOrDefault(name, otherwise);
        return otherwise;
    }

    Object getAt(int distance, String name){
        return ancestor(distance).values.get(name);
    }

    void assignAt(int distance, Token name, Object value){
        ancestor(distance).values.put(name.lexeme, value);
    }

    Environment ancestor(int distance){
        Environment environment = this;

        for(int i=0;i<distance;i++){
            environment = environment.enclosing;
        }
        return environment;
    }
}
