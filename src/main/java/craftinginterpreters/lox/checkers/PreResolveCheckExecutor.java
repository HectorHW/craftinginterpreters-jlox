package craftinginterpreters.lox.checkers;

import craftinginterpreters.lox.Stmt;

import java.util.List;

public class PreResolveCheckExecutor {
        public void check(List<Stmt> statements){
            new ThisSuperChecker().resolve(statements);
        }

}
