package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;
import java.util.*;
import java.util.Scanner;

public class StdIO extends LoxClass {

    public static final StdIO instance = new StdIO();

    static class StdIoInstance extends LoxInstance{
        protected final java.util.Scanner scanner = new java.util.Scanner(System.in);
        protected StdIoInstance(StdIO classInstance){
            super(classInstance);
        }
    }

    private StdIO(){
        super("StdIO", LoxClass.anyClass, new HashMap<>());
    }

    public static LoxClass getInstance(){
        return instance;
    }

    private void define_methods(StdIoInstance instance){
        instance.fields.put("readNum", new LoxCallable() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try{
                    java.util.Scanner scanner = instance.scanner;
                    return scanner.nextDouble();
                }catch (InputMismatchException e){
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "readnum", null, -1),
                        "failed to read number with readnum.");
                }
            }

            @Override
            public String toString(){
                return "<native fn>";
            }
        });

        instance.fields.put("readLine", new LoxCallable() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try{
                    Scanner scanner = instance.scanner;
                    return scanner.nextLine();
                }catch (InputMismatchException e){
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "readline", null, -1),
                        "failed to read with readline.");
                }
            }
            @Override
            public String toString(){
                return "<native fn>";
            }
        });

        instance.fields.put("print", new LoxCallable() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                String out = Interpreter.stringify(arguments.get(0));
                System.out.print(out);
                return out;
            }
        });

        instance.fields.put("println", new LoxCallable() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                String out = Interpreter.stringify(arguments.get(0));
                System.out.println(out);
                return out;
            }
        });
    }

    @Override
    public Set<Integer> arity() {
        return Collections.singleton(0);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {

        var instance = new StdIoInstance(this);
        define_methods(instance);
        return instance;
    }
}
