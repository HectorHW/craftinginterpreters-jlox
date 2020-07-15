package craftinginterpreters.lox;

import java.util.List;

public abstract class Stmt {
  public interface Visitor<R> {
    public R visitBlockStmt(Block stmt);
    public R visitClassStmt(Class stmt);
    public R visitExpressionStmt(Expression stmt);
    public R visitIfStmt(If stmt);
    public R visitPrintStmt(Print stmt);
    public R visitVarStmt(Var stmt);
    public R visitWhileStmt(While stmt);
    public R visitForStmt(For stmt);
    public R visitControlStatementStmt(ControlStatement stmt);
    public R visitFunctionStmt(Function stmt);
    public R visitReturnStmt(Return stmt);
  }
  public static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitBlockStmt(this);
    }
    public final List<Stmt> statements;
  }
  public static class Class extends Stmt {
    Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitClassStmt(this);
    }
    public final Token name;
    public final Expr.Variable superclass;
    public final List<Stmt.Function> methods;
  }
  public static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitExpressionStmt(this);
    }
    public final Expr expression;
  }
  public static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitIfStmt(this);
    }
    public final Expr condition;
    public final Stmt thenBranch;
    public final Stmt elseBranch;
  }
  public static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitPrintStmt(this);
    }
    public final Expr expression;
  }
  public static class Var extends Stmt {
    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitVarStmt(this);
    }
    public final Token name;
    public final Expr initializer;
  }
  public static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitWhileStmt(this);
    }
    public final Expr condition;
    public final Stmt body;
  }
  public static class For extends Stmt {
    For(Stmt init, Expr condition, Stmt body, Expr increment) {
      this.init = init;
      this.condition = condition;
      this.body = body;
      this.increment = increment;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitForStmt(this);
    }
    public final Stmt init;
    public final Expr condition;
    public final Stmt body;
    public final Expr increment;
  }
  public static class ControlStatement extends Stmt {
    ControlStatement(Token parameter) {
      this.parameter = parameter;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitControlStatementStmt(this);
    }
    public final Token parameter;
  }
  public static class Function extends Stmt {
    Function(Token name, List<Token> params, List<Stmt> body, boolean isStaticClassMethod) {
      this.name = name;
      this.params = params;
      this.body = body;
      this.isStaticClassMethod = isStaticClassMethod;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitFunctionStmt(this);
    }
    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;
    public final boolean isStaticClassMethod;
  }
  public static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitReturnStmt(this);
    }
    public final Token keyword;
    public final Expr value;
  }

  public abstract <R> R accept(Visitor<R> visitor);
}
