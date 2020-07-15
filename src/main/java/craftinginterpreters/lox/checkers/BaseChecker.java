package craftinginterpreters.lox.checkers;

import com.sun.jdi.ClassType;
import craftinginterpreters.lox.*;

import java.util.List;

public class BaseChecker implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    public BaseChecker(){ }

    public void check(List<Stmt> statements){
        for(Stmt stmt : statements){
            resolve(stmt);
        }
    }

    //сам по себе ничего не делает, упрощает написание других чекеров
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
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
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
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
        resolve(expr.body);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        resolve(stmt.statements);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        if(stmt.superclass!=null){
            resolve(stmt.superclass);
        }

        for(var method : stmt.methods){
            resolveFunction(method);
        }
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
    public Void visitVarStmt(Stmt.Var stmt) {
        if(stmt.initializer!=null){
            resolve(stmt.initializer);
        }
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
        resolveFunction(stmt);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if(stmt.value!=null){
            resolve(stmt.value);
        }
        return null;
    }

    protected void resolve(List<Stmt> statements){
        for(Stmt stmt : statements){
            resolve(stmt);
        }
    }

    protected void resolve(Expr expr){
        expr.accept(this);
    }

    protected void resolve(Stmt stmt){
        stmt.accept(this);
    }

    protected void resolveFunction(Stmt.Function function){
        resolve(function.body);
    }
}
