package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.Expr;
import craftinginterpreters.lox.Lox;
import craftinginterpreters.lox.Token;

import java.util.HashMap;
import java.util.Map;

public class UnknownVariableChecker extends UnusedVariableChecker{
    //выдавать предупреждение если переменная до этого не встречалась
    protected final Map<String, Token> globals = new HashMap<>();
    @Override
    protected void endScope(){
        scopes.pop();
    }

    @Override
    protected void declare(Token name){
        if (scopes.isEmpty()){
            globals.put(name.lexeme, name);
        }else{
            var scope = scopes.peek();
            scope.put(name.lexeme, name);
        }
    }

    //не проверяем this и super - их неопределённость является ошибкой, проверяемой ThisSuperChecker
    @Override
    public Void visitThisExpr(Expr.This expr) {
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        var variableName = expr.name;
        for(int i=scopes.size()-1; i>=0;i--){
            if(scopes.get(i).containsKey(variableName.lexeme)){
                return null;
            }
        }
        if(globals.containsKey(variableName.lexeme)) return null;
        Lox.warning(variableName, "variable reference without prior declaration is deprecated");
        return null;
    }

}
