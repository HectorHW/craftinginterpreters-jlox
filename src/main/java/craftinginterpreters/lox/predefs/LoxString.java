package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

import java.util.*;

public class LoxString extends NativeLoxClass{

    private static final LoxString classInstance = new LoxString();
    public static class LoxStringInstance extends NativeLoxInstance{
        protected final String data;
        public LoxStringInstance(String data) {
            super(classInstance);
            this.data = data;
            LoxString.define_methods(classInstance, this);
        }

        @Override
        public String toString(){
            return this.data;
        }

        @Override
        public boolean equals(Object other){
            if(!(other instanceof LoxStringInstance)) return false;
            return this.data.equals(((LoxStringInstance) other).data);
        }
    }

    public LoxString(){
        super("String", LoxClass.anyClass, new HashMap<>());
    }

    public static LoxClass getInstance(){
        return classInstance;
    }

    private static void define_methods(LoxString classinstance, LoxStringInstance instance){
        instance.fields.put("len", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return new LoxNumber.LoxNumberInstance(0.0+instance.data.length());
            }
        });

        instance.fields.put("plus_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return new LoxStringInstance(instance.data+Interpreter.stringify(arguments.get(0)));
            }
        });

        instance.fields.put("String_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return instance;
            }
        });

        instance.fields.put("g_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Predefs.requireType((LoxInstance) arguments.get(0), "expected String", classinstance);
                return LoxBoolean.LoxBooleanInstance.getInstance(
                    instance.data.compareTo(((LoxStringInstance)arguments.get(0)).data)>0);
            }
        });
        instance.fields.put("l_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Predefs.requireType((LoxInstance) arguments.get(0), "expected String", classinstance);
                return LoxBoolean.LoxBooleanInstance.getInstance(
                    instance.data.compareTo(((LoxStringInstance)arguments.get(0)).data)<0);
            }
        });
        instance.fields.put("ge_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Predefs.requireType((LoxInstance) arguments.get(0), "expected String", classinstance);
                return LoxBoolean.LoxBooleanInstance.getInstance(
                    instance.data.compareTo(((LoxStringInstance)arguments.get(0)).data)>=0);
            }
        });
        instance.fields.put("le_", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Predefs.requireType((LoxInstance) arguments.get(0), "expected String", classinstance);
                return LoxBoolean.LoxBooleanInstance.getInstance(
                    instance.data.compareTo(((LoxStringInstance)arguments.get(0)).data)<=0);
            }
        });

    }

    @Override
    public Set<Integer> arity() {
        return Collections.singleton(1);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {

        var instance = new LoxStringInstance(Interpreter.stringify(arguments.get(0)));
        define_methods(this, instance);
        return instance;
    }
}
