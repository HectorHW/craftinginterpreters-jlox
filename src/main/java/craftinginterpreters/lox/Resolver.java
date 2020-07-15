package craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//variable resolution
//public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    public class Resolver extends craftinginterpreters.lox.checkers.BaseChecker{
    //TODO обращение к локальным переменным по идексу с адресацией в массиее вместо String и HashMap
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.name);
        define(stmt.name);

        if(stmt.superclass !=null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)){
            Lox.error(stmt.superclass.name,
                "Class cannot inherit itself.");
        } // случай class A < A недопустим

        if(stmt.superclass!=null){
            resolve(stmt.superclass);
        }

        if(stmt.superclass!=null){
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for(var method : stmt.methods){
            var declaration = method.name.lexeme.equals("init") ? FunctionType.INITIALIZER : FunctionType.METHOD;

            resolveFunction(method, declaration);
        }
        endScope();

        if(stmt.superclass!=null) endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if(stmt.initializer!=null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    private void beginScope(){
        scopes.push(new HashMap<>());
    }

    private void endScope(){
        scopes.pop();
    }

    private void declare(Token name){
        if (scopes.isEmpty()) return;
        var scope = scopes.peek();
        if(scope.containsKey(name.lexeme)){
            Lox.error(name, "Variable with this name already declared in this scope");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name){
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
        // переменная не была найдена, так что положим что она глобальная
    }

    private void resolveFunction(Stmt.Function function, FunctionType type){
        beginScope();
        for(Token param: function.params){
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() &&
            scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name,
                "Cannot read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAnonFunExpr(Expr.AnonFun expr) {
        beginScope();
        for(Token param: expr.params){
            declare(param);
            define(param);
        }
        resolve(expr.body);
        endScope();
        return null;
    }

    private void createVariableEntry(Map<String, Map<String, Object>> field, Token variable ){
        var hm = new HashMap<String, Object>();
        hm.put("defined", false);
        hm.put("used", variable);
        field.put(variable.lexeme, hm);
    }
}
