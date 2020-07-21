package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.Interpreter;
import craftinginterpreters.lox.LoxClass;

import java.util.*;

public class LoxNumber extends NativeLoxClass{
    private static final LoxNumber classInstance = new LoxNumber();

    public static class LoxNumberInstance extends NativeLoxInstance{
        double data;
        public LoxNumberInstance(double data) {
            super(classInstance);
            this.data = data;
            LoxNumber.define_methods(classInstance, this);
        }

        @Override
        public String toString(){
            return Double.toString(this.data);
        }

        @Override
        public boolean equals(Object other){
            if(!(other instanceof LoxNumberInstance)) return false;
            return this.data == ((LoxNumberInstance) other).data;
        }


    }


    private LoxNumber() {
        super("Number", LoxClass.anyClass, new HashMap<>());
    }
    public static LoxClass getInstance(){
        return classInstance;
    }

    static void define_methods(LoxNumber classInstance, LoxNumberInstance instance){
        //TODO
    }

    @Override
    public Set<Integer> arity() {
        return Collections.singleton(1);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Predefs.requireType( arguments.get(0), "expected String or Double", LoxString.getInstance(), LoxNumber.getInstance());
        LoxNumberInstance instance;
        if(arguments.get(0) instanceof LoxNumberInstance){
            instance = new LoxNumberInstance(((LoxNumberInstance)arguments.get(0)).data);
        }else{
            instance = new LoxNumberInstance(Double.parseDouble(((LoxString.LoxStringInstance)arguments.get(0)).data));
        }

        define_methods(this, instance);
        return instance;
    }
}
