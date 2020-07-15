package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.*;

public class ThisSuperChecker extends BaseChecker{
    private enum ClassType{
        NONE, CLASS, SUBCLASS
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
            System.out.println("check");
            return null;
        }
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if(classType== ClassType.NONE){
            Lox.error(expr.keyword, "Cannot use `super` outside of a class.");
            System.out.println("check");
        }else if(classType!= ClassType.SUBCLASS){
            Lox.error(expr.keyword, "Cannot use `super` in a class with no superclass.");
            System.out.println("check");
        }
        return null;
    }
}
