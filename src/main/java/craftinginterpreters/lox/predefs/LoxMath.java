package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class LoxMath extends NativeLoxClass {
    private static LoxMath classInstance = new LoxMath();
    public LoxMath() {
        super("Math", LoxClass.anyClass, new HashMap<>());
        define_methods(this);
    }
    public static LoxMath getInstance(){return classInstance;}

    private void define_methods(LoxMath classInstance){
        classInstance.fields.put("pow", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object argument1 = arguments.get(0);
                Object argument2 = arguments.get(1);

                if(argument1 instanceof Double && argument2 instanceof Double){
                    return Math.pow((Double)argument1, (Double)argument2);
                }
                throw new RuntimeError(new Token(TokenType.IDENTIFIER, "pow", null, -1),
                    "argments must be numbers");
            }
        });

        classInstance.fields.put("abs", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object argument1 = arguments.get(0);

                if(argument1 instanceof Double){
                    return Math.abs((Double)argument1);
                }
                throw new RuntimeError(new Token(TokenType.IDENTIFIER, "pow", null, -1),
                    "argments must be numbers");
            }
        });
    }


    @Override
    public Set<Integer> arity() {
        return Collections.singleton(0);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "Math", null, -1),
            "cannot create instance of this class");
    }
}
