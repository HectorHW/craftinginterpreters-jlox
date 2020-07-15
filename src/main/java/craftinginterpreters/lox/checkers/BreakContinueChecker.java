package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.Expr;
import craftinginterpreters.lox.Lox;
import craftinginterpreters.lox.Stmt;

public class BreakContinueChecker extends BaseChecker{
    private enum LoopType{
        NONE, LOOP
    }
    LoopType loop = LoopType.NONE;

    @Override
    public Void visitControlStatementStmt(Stmt.ControlStatement stmt) {
        if(loop==LoopType.NONE){
            Lox.error(stmt.parameter, "loop control statement outside of loop.");
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        if(stmt.init!=null) resolve(stmt.init);
        if(stmt.condition!=null) resolve(stmt.condition);
        if(stmt.increment!=null) resolve(stmt.increment);

        var enclosing_loop = loop;
        loop = LoopType.LOOP;
        resolve(stmt.body);
        loop = enclosing_loop;
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        var enclosing_loop = loop;
        loop = LoopType.LOOP;
        resolve(stmt.body);
        loop = enclosing_loop;
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        var enclosing_loop = loop;
        loop = LoopType.NONE;
        resolve(stmt.body);
        loop = enclosing_loop;
        return null;
    }

    @Override
    public Void visitAnonFunExpr(Expr.AnonFun expr) {
        var enclosing_loop = loop;
        loop = LoopType.NONE;
        resolve(expr.body);
        loop = enclosing_loop;
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        var enclosing_loop = loop;
        loop = LoopType.NONE;
        super.visitClassStmt(stmt);
        loop = enclosing_loop;
        return null;
    }


}
