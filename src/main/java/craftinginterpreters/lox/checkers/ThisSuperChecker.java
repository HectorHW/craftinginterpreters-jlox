package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.*;

public class ThisSuperChecker extends BaseChecker{
    private enum ClassType{
        NONE, CLASS, SUBCLASS, STATIC_METHOD_SCOPE
    }
    private ClassType classType = ClassType.NONE;

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        var enclosingClass = classType;
        classType = ClassType.CLASS;

        if(stmt.superclass!=null){
            classType = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        for(var method : stmt.methods){
            resolveFunction(method);
        }
        classType = enclosingClass;
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if(classType== ClassType.NONE){
            Lox.error(expr.keyword, "Cannot use `this` outside of a class.");
            return null;
        }else if(classType==ClassType.STATIC_METHOD_SCOPE){
            Lox.error(expr.keyword, "Cannot use `this` in static method.");
            return null;
        }
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if(classType== ClassType.NONE){
            Lox.error(expr.keyword, "Cannot use `super` outside of a class.");
        }else if(classType== ClassType.CLASS){
            Lox.error(expr.keyword, "Cannot use `super` in a class with no superclass.");
        }else if(classType==ClassType.STATIC_METHOD_SCOPE){
            Lox.error(expr.keyword, "Cannot use `super` in static method.");
            return null;
        }
        return null;
    }

    @Override
    protected void resolveFunction(Stmt.Function function){
        var enclosingClass = classType;
        if (function.isStaticClassMethod) classType = ClassType.STATIC_METHOD_SCOPE;
        //внутри static методов нельзя использовать this и super
        resolve(function.body);
        classType = enclosingClass;
    }
}
