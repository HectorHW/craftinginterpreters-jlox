package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static craftinginterpreters.lox.predefs.Predefs.Arities.*;


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
                return TWO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object argument1 = arguments.get(0);
                Object argument2 = arguments.get(1);
                Predefs.requireType(argument1, "Number expected", LoxNumber.getInstance());
                Predefs.requireType(argument2, "Number expected", LoxNumber.getInstance());
                return new LoxNumber.LoxNumberInstance(
                    Math.pow(((LoxNumber.LoxNumberInstance)argument1).data, ((LoxNumber.LoxNumberInstance)argument2).data)
                );
            }
        });
    }


    @Override
    public Set<Integer> arity() {
        return ZERO_ARGUMENTS;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "Math", null, -1),
            "cannot create instance of this class");
    }
}
