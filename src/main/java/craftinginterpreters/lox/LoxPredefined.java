
package craftinginterpreters.lox;

import craftinginterpreters.lox.predefs.Predefs;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class LoxPredefined {
    static void bake(Interpreter interpreter){
        defineClock(interpreter.globals);
        defineSleep(interpreter.globals);
        defineReadnum(interpreter.globals);
        defineReadline(interpreter.globals);
        definePow(interpreter.globals);
        defineClasses(interpreter.globals);
        defineType(interpreter.globals);
        defineArity(interpreter.globals);
        defineAssert(interpreter.globals);
        defineImport(interpreter.globals);
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
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "sleep", null, -1),
                        "argument must be a number");
                }

                try {
                    long time = Math.round((double)arguments.get(0));
                    if(time<0){
                        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "sleep", null, -1),
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
        env.define("readnum", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try{
                    Scanner scanner =
                        (Scanner) interpreter.globals.getOrDefault(
                            new Token(TokenType.IDENTIFIER, "+StdIn+", null, -1),
                            new java.util.Scanner(System.in));
                    interpreter.globals.define("+StdIn+", scanner);
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
    }

    static void defineReadline(Environment env){
        env.define("readline", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try{
                    Scanner scanner =
                        (Scanner) interpreter.globals.getOrDefault(
                            new Token(TokenType.IDENTIFIER, "+StdIn+", null, -1),
                            new java.util.Scanner(System.in));
                    interpreter.globals.define("+StdIn+", scanner);
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
    }

    static void definePow(Environment env){
        env.define("pow", new LoxCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try{
                    return Math.pow((Double) arguments.get(0), (Double)(arguments.get(1)));
                }catch (ClassCastException e){
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "pow", null, -1),
                        "arguments must be numbers.");
                }
            }
            @Override
            public String toString(){
                return "<native fn>";
            }
        });
    }
    
    static void defineClasses(Environment env) {
        env.define("String", new LoxClass("String", LoxClass.anyClass, new HashMap<>()));
        env.define("Number", new LoxClass("Number", LoxClass.anyClass, new HashMap<>()));
        env.define("Class", LoxClass.anyClass);
        env.define("Function", new LoxClass("Function", LoxClass.anyClass, new HashMap<>()));
        env.define("Object", new LoxClass("Object", LoxClass.anyClass, new HashMap<>()));
        env.define("Boolean", new LoxClass("Boolean", LoxClass.anyClass, new HashMap<>()));
        env.define("Nil", new LoxClass("Nil", LoxClass.anyClass, new HashMap<>()));

    }

    static void defineType(Environment env){
        env.define("type", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object argument = arguments.get(0);
                if(argument instanceof String){
                    return env.get(new Token(TokenType.IDENTIFIER, "String", null, -1));
                }
                if(argument instanceof Double){
                    return env.get(new Token(TokenType.IDENTIFIER, "Number", null, -1));
                }
                if(argument instanceof LoxClass){
                    return env.get(new Token(TokenType.IDENTIFIER, "Class", null, -1));
                }
                if(argument instanceof LoxFunction){
                    return env.get(new Token(TokenType.IDENTIFIER, "Function", null, -1));
                }
                if(argument instanceof LoxInstance){
                    return ((LoxInstance)argument).getLoxClass();
                }
                if(argument instanceof Boolean){
                    return env.get(new Token(TokenType.IDENTIFIER, "Boolean", null, -1));
                }
                if(argument==null){
                    return env.get(new Token(TokenType.IDENTIFIER, "Nil", null, -1));
                }

                throw new RuntimeError(new Token(TokenType.IDENTIFIER, "type", null, -1),
                    "could not identify type.");
                
            }
            @Override
            public String toString(){
                return "<native fn>";
            }
        });
    }

    static void defineArity(Environment env){
        env.define("arity", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try{
                    LoxFunction ff = (LoxFunction)arguments.get(0);
                    return ff.arity();
                }catch (ClassCastException e){
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "arity", null, -1),
                        "arguments must be numbers.");
                }
            }
            @Override
            public String toString(){
                return "<native fn>";
            }
        });
    }

    static void defineAssert(Environment env){
        env.define("assert", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object argument = arguments.get(0);
                if(!interpreter.isTruthy(argument)) throw new RuntimeError(new Token(TokenType.IDENTIFIER, "assert", null, -1),
                    "assertion error.");
                return argument;
            }
        });
    }

    static void defineImport(Environment env){
        env.define("import", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if(!(arguments.get(0) instanceof String)){
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "import", null, -1),
                        "argument must be a string.");
                }

                String argument = (String)arguments.get(0);

                String[] subparams = argument.split("\\.");

                if(subparams.length==2 && subparams[0].equals("predef")){
                    return Predefs.getPredef(subparams[1]);
                }

                if(subparams.length==1){
                    try{
                        return env.get(new Token(TokenType.IDENTIFIER, subparams[0], subparams[0], -1));
                    }catch (RuntimeError e){
                        throw new RuntimeError(new Token(TokenType.IDENTIFIER, subparams[0], subparams[0], -1),
                            "Failed to resolve name "+argument+".");
                    }

                }
                if(subparams.length>=2){
                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<subparams.length-2;i++){
                        sb.append(subparams[i]);
                        sb.append("/");
                    }
                    String path = sb.toString() + subparams[subparams.length-2] + ".jlox";
                    String fname = subparams[subparams.length-1];


                    if(interpreter.importer_files.containsKey(path)){
                        try{
                            return interpreter.importer_files.get(path).get(new Token(TokenType.IDENTIFIER, fname, fname, -1));
                        }catch (RuntimeError e){
                            throw new RuntimeError(new Token(TokenType.IDENTIFIER, subparams[0], subparams[0], -1),
                                "Failed to resolve name "+argument+".");
                        }

                    }else{
                        Environment res;
                        try{
                            res = executeForEnvironment(path);

                        }catch (IOException e){
                            throw new RuntimeError(new Token(TokenType.IDENTIFIER, subparams[0], subparams[0], -1),
                                "Failed to find file "+path+" while importing.");
                        }catch (Parser.ParseError | Resolver.ResolveError | RuntimeError e){
                            throw new RuntimeError(new Token(TokenType.IDENTIFIER, subparams[0], subparams[0], -1),
                                "Failed to execute file "+path+" while importing.");
                        }

                        interpreter.importer_files.put(path, res);
                        try{
                            return res.get(new Token(TokenType.IDENTIFIER, fname, fname, -1));
                        }catch (RuntimeError e){
                            throw new RuntimeError(new Token(TokenType.IDENTIFIER, subparams[0], subparams[0], -1),
                                "Could not find name "+fname+" in file "+path+" while importing.");
                        }

                    }
                }else{
                    return null;
                }
            }

            private Environment executeForEnvironment(String filename) throws IOException{
                byte[] bytes = Files.readAllBytes(Paths.get(filename));
                return Lox.runForEnvironment(new String(bytes, Charset.defaultCharset()));
            }

        });
    }
}