package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.Stmt;

import java.util.List;

public class CheckExecutor {
    public void check(List<Stmt> statements){
        new ReturnChecker().resolve(statements);
        new BreakContinueChecker().resolve(statements);
        new UnusedVariableChecker().resolve(statements);
    }
}
