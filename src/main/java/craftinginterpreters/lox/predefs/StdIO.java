package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;
import java.util.*;
import java.util.Scanner;

public class StdIO extends NativeLoxClass {


    static class StdIoInstance extends NativeLoxInstance{
        protected final java.util.Scanner scanner = new java.util.Scanner(System.in);
        protected StdIoInstance(StdIO classInstance){
            super(classInstance);
        }
    }

    private StdIO(){
        super("StdIO", LoxClass.anyClass, new HashMap<>());
    }

    public static LoxClass getInstance(){
        return new StdIO();
    }

    private void define_methods(StdIO classinstance, StdIoInstance instance){
        classinstance.methods.put("readnum", new NativeLoxFunction() {
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

        classinstance.methods.put("readln", new NativeLoxFunction() {
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
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "readln", null, -1),
                        "failed to read with readline.");
                }
            }
            @Override
            public String toString(){
                return "<native fn>";
            }
        });

        classinstance.methods.put("print", new NativeLoxFunction() {
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

        classinstance.methods.put("println", new NativeLoxFunction() {
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
        define_methods(this, instance);
        return instance;
    }
}
