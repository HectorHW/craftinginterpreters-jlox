
package craftinginterpreters.lox;

import craftinginterpreters.lox.predefs.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LoxPredefined {

    public static HashSet<String> predefined_names = new HashSet<>();
    static {
        predefined_names.add("clock");
        predefined_names.add("sleep");
        predefined_names.add("type");
        predefined_names.add("arity");
        predefined_names.add("assert");
        predefined_names.add("import");

    }

    static void bake(Interpreter interpreter){
        defineClock(interpreter.globals);
        defineSleep(interpreter.globals);
        defineClasses(interpreter.globals);
        defineType(interpreter.globals);
        defineArity(interpreter.globals);
        defineAssert(interpreter.globals);
        defineImport(interpreter.globals);
    }

    static void defineClock(Environment env){
        env.define("clock", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis()/1000.0;
            }
            @Override
            public String toString(){return "<native fn>";}
        });
    }

    static void defineSleep(Environment env) {
        env.define("sleep", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
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
    
    static void defineClasses(Environment env) {
        env.define("Class", LoxClass.anyClass);
        env.define("Function", new LoxClass("Function", LoxClass.anyClass, new HashMap<>()));
        env.define("Object", new LoxClass("Object", LoxClass.anyClass, new HashMap<>()));
        //env.define("Boolean", new LoxClass("Boolean", LoxClass.anyClass, new HashMap<>()));
        env.define("Nil", new LoxClass("Nil", LoxClass.anyClass, new HashMap<>()));

    }

    static void defineType(Environment env){
        env.define("type", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
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
                if(argument instanceof NativeLoxFunction){
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
        env.define("arity", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
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
        env.define("assert", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
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
        env.define("import", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if(!(arguments.get(0) instanceof LoxString.LoxStringInstance)){
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "import", null, -1),
                        "argument must be a string.");
                }

                String argument = arguments.get(0).toString();

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