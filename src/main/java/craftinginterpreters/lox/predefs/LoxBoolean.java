package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.Interpreter;
import craftinginterpreters.lox.LoxClass;
import craftinginterpreters.lox.LoxInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static craftinginterpreters.lox.predefs.Predefs.Arities.*;

public class LoxBoolean extends NativeLoxClass{
    private static final LoxBoolean classInstance = new LoxBoolean();
    public static final LoxBooleanInstance TRUE = new LoxBooleanInstance(true);
    public static final LoxBooleanInstance FALSE = new LoxBooleanInstance(false);
    private LoxBoolean() {
        super("Boolean", LoxClass.anyClass, new HashMap<>());
    }

    public static LoxBoolean getInstance(){return classInstance;}
    public static class LoxBooleanInstance extends NativeLoxInstance{
        public final boolean value;

        public LoxBooleanInstance(boolean value) {
            super(classInstance);
            this.value = value;
            LoxBoolean.define_methods(classInstance, this);
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }

        @Override
        public boolean equals(Object other){
            if(!(other instanceof LoxBooleanInstance)) return false;
            return this.value==((LoxBooleanInstance) other).value;
        }

        public LoxBooleanInstance or(LoxBooleanInstance other){
            return this.value||other.value ? TRUE : FALSE;
        }
        public LoxBooleanInstance and(LoxBooleanInstance other){
            return this.value&&other.value ? TRUE : FALSE;
        }
        public LoxBooleanInstance not(){
            return !this.value ? TRUE : FALSE;
        }

        public static LoxBooleanInstance getInstance(boolean value){
            return  value ? TRUE : FALSE;
        }

        public static LoxBooleanInstance equality(Object left, Object right){
            return left.equals(right) ? TRUE : FALSE;
        }
    }

    static void define_methods(LoxBoolean classInstance, LoxBooleanInstance instance){
        instance.fields.put("String_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ZERO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return new LoxString.LoxStringInstance(instance.toString());
            }
        });

        instance.fields.put("Bool_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ZERO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return instance.value ? TRUE : FALSE;
            }
        });

        instance.fields.put("unarybang_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ZERO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return instance.value ? FALSE : TRUE;
            }
        });

    }

    @Override
    public Set<Integer> arity() {
        return ONE_ARGUMENT;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return interpreter.isTruthy(arguments.get(0)) ? TRUE : FALSE;
    }

}
