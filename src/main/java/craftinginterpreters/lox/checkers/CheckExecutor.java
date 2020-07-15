package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.Stmt;

import java.util.List;

public class CheckExecutor {
    ReturnChecker rc;
    public CheckExecutor(){
        rc = new ReturnChecker();
    }

    public void check(List<Stmt> statements){
        rc.resolve(statements);
        new BreakContinueChecker().resolve(statements);
    }
}
