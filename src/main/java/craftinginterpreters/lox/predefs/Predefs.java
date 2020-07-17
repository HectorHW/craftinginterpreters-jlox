package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

public class Predefs {

    public static Object getPredef(String name){
        if(name.equals("StdIO")) return StdIO.getInstance();
        if(name.equals("FileIO")) return FileIO.getInstance();
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, name, null, -1), "unknown predefined object.");
    }

    public static LoxInstance getObjectRefFromMethod(LoxFunction method){
        return (LoxInstance) method.closure.get("this");
    }
}
