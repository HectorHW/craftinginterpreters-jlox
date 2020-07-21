package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

import java.util.*;

import static craftinginterpreters.lox.predefs.Predefs.Arities.*;
import static craftinginterpreters.lox.predefs.Predefs.requireType;

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
            String text = Double.toString(data);
            if(text.endsWith(".0")){
                text = text.substring(0, text.length()-2);
            }
            return text;
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
        instance.fields.put("plus_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected String or Number", LoxString.getInstance(), LoxNumber.getInstance());
                LoxInstance argument = (LoxInstance)arguments.get(0);
                if(argument instanceof LoxString.LoxStringInstance){
                    return new LoxString.LoxStringInstance(instance.toString()+((LoxString.LoxStringInstance) argument).data);
                }else if(argument instanceof LoxNumber.LoxNumberInstance){
                    return new LoxNumberInstance(instance.data+((LoxNumberInstance) argument).data);
                }
                return null;
            }
        });

        instance.fields.put("minus_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected Number", LoxNumber.getInstance());
                var argument = (LoxNumberInstance)arguments.get(0);
                return new LoxNumberInstance(instance.data-argument.data);
            }
        });

        instance.fields.put("star_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected Number", LoxNumber.getInstance());
                var argument = (LoxNumberInstance)arguments.get(0);
                return new LoxNumberInstance(instance.data*argument.data);
            }
        });

        instance.fields.put("slash_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected Number", LoxNumber.getInstance());
                var argument = (LoxNumberInstance)arguments.get(0);
                if(argument.data==0.0) throw new RuntimeError(
                    new Token(TokenType.SLASH, "/", null, -1), "Zero division");
                return new LoxNumberInstance(instance.data/argument.data);
            }
        });

        instance.fields.put("g_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected Number", LoxNumber.getInstance());
                var argument = (LoxNumberInstance)arguments.get(0);
                return instance.data>argument.data;
            }
        });

        instance.fields.put("ge_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected Number", LoxNumber.getInstance());
                var argument = (LoxNumberInstance)arguments.get(0);
                return instance.data>=argument.data;
            }
        });

        instance.fields.put("l_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected Number", LoxNumber.getInstance());
                var argument = (LoxNumberInstance)arguments.get(0);
                return instance.data<argument.data;
            }
        });

        instance.fields.put("le_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ONE_ARGUMENT;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                requireType(arguments.get(0), "expected Number", LoxNumber.getInstance());
                var argument = (LoxNumberInstance)arguments.get(0);
                return instance.data<=argument.data;
            }
        });

        instance.fields.put("String_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ZERO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return new LoxString.LoxStringInstance(String.valueOf(instance.data));
            }
        });

        instance.fields.put("Number_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ZERO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return instance;
            }
        });

        instance.fields.put("Boolean_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ZERO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return LoxBoolean.LoxBooleanInstance.getInstance(instance.data!=0.0 && !Double.isNaN(instance.data));
            }
        });

        instance.fields.put("unaryminus_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return ZERO_ARGUMENTS;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return new LoxNumberInstance(-instance.data);
            }
        });


    }

    @Override
    public Set<Integer> arity() {
        return ONE_ARGUMENT;
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
