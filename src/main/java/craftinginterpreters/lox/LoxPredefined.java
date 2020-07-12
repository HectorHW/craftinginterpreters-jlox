package craftinginterpreters.lox;

import java.util.InputMismatchException;
import java.util.List;

public class LoxPredefined {
    static void bake(Interpreter interpreter){
        defineClock(interpreter.globals);
        defineSleep(interpreter.globals);
        defineReadnum(interpreter.globals);
        defineReadline(interpreter.globals);
    }

    static void defineClock(Environment env){
        env.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis()/1000.0;
            }
            @Override
            public String toString(){return "<native fn>";}
        });
    }

    static void defineSleep(Environment env) {
        env.define("sleep", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if(!(arguments.get(0) instanceof Double)){
                    throw new RuntimeError(new Token(TokenType.NIL, "", null, -1),
                        "argument must be a number");
                }

                try {
                    long time = Math.round((double)arguments.get(0));
                    if(time<0){
                        throw new RuntimeError(new Token(TokenType.NIL, "", null, -1),
                            "time cannot be negative.");
                    }
                    Thread.sleep(time);
                } catch (InterruptedException ignored) {}
                return null;
            }
            @Override
            public String toString(){return "<native fn>";}
        });
    }

    static void defineReadnum(Environment env){
        env.define("readnum", (LoxMacro) (interpreter) -> {
            var scanner = new java.util.Scanner(System.in);
            try{
                return scanner.nextDouble();
            }catch (InputMismatchException e){
                throw new RuntimeError(new Token(TokenType.IDENTIFIER, "readnum", null, -1),
                    "failed to read number with readnum.");
            }

        });
    }

    static void defineReadline(Environment env){
        env.define("readline", (LoxMacro) (interpreter) -> {
            var scanner = new java.util.Scanner(System.in);
            return scanner.nextLine();
        });
    }

}
