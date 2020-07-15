package craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//variable resolution
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    //TODO обращение к локальным переменным по идексу с адресацией в массиее вместо String и HashMap
    private final Interpreter interpreter;
    private final Stack<Map<String, Map<String, Object>>> scopes = new Stack<>();
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
            createVariableEntry(scopes.peek(), new Token(TokenType.SUPER, "super", null, stmt.name.line));
        }

        beginScope();
        createVariableEntry(scopes.peek(), new Token(TokenType.THIS, "this", null, stmt.name.line));

        for(var method : stmt.methods){
            var declaration = method.name.lexeme.equals("init") ? FunctionType.INITIALIZER : FunctionType.METHOD;

            resolveFunction(method, declaration);
        }
        endScope();

        if(stmt.superclass!=null) endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch!=null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
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
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        if(stmt.init!=null) resolve(stmt.init);
        if(stmt.condition!=null) resolve(stmt.condition);
        if(stmt.increment!=null) resolve(stmt.increment);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitControlStatementStmt(Stmt.ControlStatement stmt) {
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if(stmt.value!=null){
            resolve(stmt.value);
        }
        return null;
    }

    void resolve(List<Stmt> statements){
        for(Stmt stmt : statements){
            resolve(stmt);
        }
    }

    private void resolve(Stmt stmt){
        stmt.accept(this);
    }
    private void resolve(Expr expr){
        expr.accept(this);
    }

    private void beginScope(){
        scopes.push(new HashMap<>());
    }

    private void endScope(){
        for(var record : scopes.peek().entrySet()){
            if(record.getValue().get("used")!=null
                && !record.getKey().equals("this")
                && !record.getKey().equals("super")
            ){
                Lox.warning((Token)record.getValue().get("used"), "Unused variable "+((Token) record.getValue().get("used")).lexeme);
            }
        }

        scopes.pop();
    }

    private void declare(Token name){
        if (scopes.isEmpty()) return;
        var scope = scopes.peek();
        if(scope.containsKey(name.lexeme)){
            Lox.error(name, "Variable with this name already declared in this scope");
        }

        createVariableEntry(scope, name);
    }

    private void define(Token name){
        if(scopes.isEmpty()) return;
        //scopes.peek().put(name.lexeme, true);
        scopes.peek().get(name.lexeme).put("defined", true);
    }

    private void resolveLocal(Expr expr, Token name){
        for(int i=scopes.size()-1;i>=0;i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size() -1 -i);
                if(!scopes.isEmpty()){
                    scopes.get(i).get(name.lexeme).put("used", null);
                }
                return;
            }
        }
        //Not found. Assume it is global
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
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.left);
        resolve(expr.middle);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
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
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if(!scopes.isEmpty() && scopes.peek().containsKey(expr.name.lexeme) &&
        scopes.peek().get(expr.name.lexeme).get("defined")==Boolean.FALSE) {
            //для кода вида var a; {var a = a;}
            Lox.error(expr.name,
                "Cannot read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.calee);

        for(Expr argument : expr.arguments){
            resolve(argument);
        }
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

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    private void createVariableEntry(Map<String, Map<String, Object>> field, Token variable ){
        var hm = new HashMap<String, Object>();
        hm.put("defined", false);
        hm.put("used", variable);
        field.put(variable.lexeme, hm);
    }
}
