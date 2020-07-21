package craftinginterpreters.lox.predefs;

import craftinginterpreters.lox.*;

public class Predefs {

    public static Object getPredef(String name){
        return switch (name) {
            case "StdIO" -> StdIO.getInstance();
            case "FileIO" -> FileIO.getInstance();
            case "String" -> LoxString.getInstance();
            case "Number" -> LoxNumber.getInstance();
            default -> throw new RuntimeError(new Token(TokenType.IDENTIFIER, name, null, -1), "unknown predefined object.");
        };
    }

    public static LoxInstance getObjectRefFromMethod(LoxFunction method){
        return (LoxInstance) method.closure.get("this");
    }

    public static void requireType(Object object, String error, LoxClass... types){
        if(object instanceof LoxInstance){
            var oobject = (LoxInstance)object;
            for(var type : types){
                if(oobject.getLoxClass().equals(type)) return;
            }
        }

        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "typing", null, -1), error);
    }
}
