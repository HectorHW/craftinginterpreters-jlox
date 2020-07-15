package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.*;

public class ReturnChecker extends BaseChecker{
    private enum FunctionType{
        NONE, FUNCTION_OR_METHOD, INIT_METHOD
    }
    private FunctionType function = FunctionType.NONE;
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {

        if(function==FunctionType.NONE){
            Lox.error(stmt.keyword, "Cannot return from top-level code.");
            return null;
        }

        if(stmt.value!=null){
            //использование return something; в инициализаторе запрещено
            // но просто return; в инициализаторе имеет смысл для досрочного выхода
            if(function==FunctionType.INIT_METHOD){
                Lox.error(stmt.keyword, "Cannot return value from initializer.");
            }
            resolve(stmt.value);
        }
        return null;
    }

    public Void visitFunctionStmt(Stmt.Function stmt) {
        var enclosingFunction = function;
        function = FunctionType.FUNCTION_OR_METHOD;
        resolve(stmt.body);
        function = enclosingFunction;
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {

        for(var method : stmt.methods){
            var declaration = method.name.lexeme.equals("init") ? FunctionType.INIT_METHOD : FunctionType.FUNCTION_OR_METHOD;
            var enclosingFunction = function;
            function = declaration;
            resolve(method.body);
            function = enclosingFunction;
        }
        return null;
    }

    @Override
    public Void visitAnonFunExpr(Expr.AnonFun expr) {
        var enclosingFunction = function;
        function = FunctionType.FUNCTION_OR_METHOD;
        resolve(expr.body);
        function = enclosingFunction;
        return  null;
    }
}