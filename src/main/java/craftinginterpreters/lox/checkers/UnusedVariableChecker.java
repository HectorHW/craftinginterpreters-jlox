package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class UnusedVariableChecker extends BaseChecker{
    protected final Stack<Map<String, Token>> scopes = new Stack<>();

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    protected void beginScope(){
        scopes.push(new HashMap<>());
    }

    protected void endScope(){
        for(var record : scopes.peek().entrySet()){
            if(record.getValue()!=null
                && !record.getKey().equals("this")
                && !record.getKey().equals("super")
            ){
                Lox.warning(record.getValue(), "Unused variable "+record.getValue().lexeme);
            }
        }

        scopes.pop();
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.name);

        if(stmt.superclass!=null){
            resolve(stmt.superclass);
        }

        beginScope();

        for(var method : stmt.methods){
            resolveFunction(method);
        }
        endScope();

        return null;
    }

    protected void declare(Token name){
        if (scopes.isEmpty()) return; //не проверяем глобальные имена
        var scope = scopes.peek();
        scope.put(name.lexeme, name);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if(stmt.initializer!=null){
            resolve(stmt.initializer);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        resolveFunction(stmt);
        return null;
    }

    @Override
    protected void resolveFunction(Stmt.Function function){
        beginScope();
        for(Token param: function.params){
            declare(param);
        }
        resolve(function.body);
        endScope();
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        markUsed(expr.name);
        return null;
    }

    @Override
    public Void visitAnonFunExpr(Expr.AnonFun expr) {
        beginScope();
        for(Token param: expr.params){
            declare(param);
        }
        resolve(expr.body);
        endScope();
        return null;
    }

    private void markUsed(Token variableName){
        if(scopes.isEmpty()) return;
        for(int i=scopes.size()-1; i>=0;i--){
            if(scopes.get(i).containsKey(variableName.lexeme)){
                scopes.get(i).put(variableName.lexeme, null);
                return;
            }
        }
    }


}
