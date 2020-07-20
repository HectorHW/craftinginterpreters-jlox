package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Scanner;

public class FileIO extends NativeLoxClass {

    private static FileIO classInstance = new FileIO();

    static class FileIoInstance extends NativeLoxInstance{

        enum FileIOMode{
            READ, WRITE
        }
        protected java.util.Scanner scanner = null;
        protected PrintWriter writer = null;
        protected final FileIOMode mode;
        protected final File file;
        protected FileIoInstance(FileIO classInstance, String path, String mode){
            super(classInstance);
            try{
                if(mode.equals("r")){
                    this.mode = FileIOMode.READ;
                    this.file = new File(path);
                    this.scanner = new Scanner(this.file);
                }else if(mode.equals("w")){
                    this.mode = FileIOMode.WRITE;
                    this.file = new File(path);
                    this.writer = new PrintWriter(this.file);
                }else{
                    throw new RuntimeError(new Token(TokenType.IDENTIFIER, "FileIO", null, -1),
                        "unknown FileIO mode: "+mode);
                }
            }catch (IOException e){
                throw new RuntimeError(new Token(TokenType.IDENTIFIER, "FileIO", null, -1),
                    "error while creating FileIO object: "+e.getMessage());
            }
        }
    }

    private FileIO(){
        super("FileIO", LoxClass.anyClass, new HashMap<>());
    }

    public static LoxClass getInstance(){
        return classInstance;
    }

    private void define_methods(FileIO classinstance, FileIoInstance objectinstance){

        objectinstance.fields.put("getMode", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return switch (objectinstance.mode){
                    case READ -> new LoxString.LoxStringInstance("r");
                    case WRITE -> new LoxString.LoxStringInstance("w");
                };
            }

            @Override
            public String toString(){
                return "<native fn>";
            }
        });


        objectinstance.fields.put("close", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                switch (objectinstance.mode){
                    case READ -> objectinstance.scanner.close();
                    case WRITE -> objectinstance.writer.close();
                }
                return null;
            }

            @Override
            public String toString(){
                return "<native fn>";
            }
        });

        objectinstance.fields.put("print", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                switch (objectinstance.mode) {
                    case READ -> throw new RuntimeError(new Token(TokenType.IDENTIFIER, "print", null, -1),
                        "FileIO in reading mode.");
                    case WRITE -> objectinstance.writer.print(Interpreter.stringify(arguments.get(0)));
                }
                return null;
            }

            @Override
            public String toString(){
                return "<native fn>";
            }
        });

        objectinstance.fields.put("println", new NativeLoxFunction() {

            @Override
            public Set<Integer> arity() {
                return Collections.singleton(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                switch (objectinstance.mode) {
                    case READ -> throw new RuntimeError(new Token(TokenType.IDENTIFIER, "println", null, -1),
                        "FileIO in reading mode.");
                    case WRITE -> objectinstance.writer.println(Interpreter.stringify(arguments.get(0)));
                }

                return null;
            }

            @Override
            public String toString(){
                return "<native fn>";
            }
        });

        objectinstance.fields.put("readln", new NativeLoxFunction() {
            @Override
            public Set<Integer> arity() {
                return Collections.singleton(0);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return switch (objectinstance.mode) {
                    case WRITE -> throw new RuntimeError(new Token(TokenType.IDENTIFIER, "readln", null, -1),
                        "FileIO in reading mode.");
                    case READ -> objectinstance.scanner.nextLine();
                };
            }

            @Override
            public String toString(){
                return "<native fn>";
            }
        });
    }

    @Override
    public Set<Integer> arity() {
        return Collections.singleton(2);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        if(arguments.get(0) instanceof LoxString.LoxStringInstance && arguments.get(1) instanceof LoxString.LoxStringInstance){
            var fileIoInstance = new FileIoInstance(this, arguments.get(0).toString(), arguments.get(1).toString());
            define_methods(this, fileIoInstance);
            return fileIoInstance;
        }else{
            throw new RuntimeError(new Token(TokenType.IDENTIFIER, "FileIO", null, -1),
                "arguments must be strings.");
        }


    }
}
