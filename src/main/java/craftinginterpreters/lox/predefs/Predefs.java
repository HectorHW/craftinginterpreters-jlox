package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.RuntimeError;
import craftinginterpreters.lox.Token;
import craftinginterpreters.lox.TokenType;

public class Predefs {

    public static Object getPredef(String name){
        if(name.equals("StdIO")) return StdIO.getInstance();

        throw new RuntimeError(new Token(TokenType.IDENTIFIER, name, null, -1), "unknown predefined object.");
    }
}
