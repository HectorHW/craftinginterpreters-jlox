package craftinginterpreters.lox;

import java.util.List;

public abstract class Expr {
  public interface Visitor<R> {
    public R visitAssignExpr(Assign expr);
    public R visitBinaryExpr(Binary expr);
    public R visitTernaryExpr(Ternary expr);
    public R visitGroupingExpr(Grouping expr);
    public R visitLiteralExpr(Literal expr);
    public R visitLogicalExpr(Logical expr);
    public R visitSetExpr(Set expr);
    public R visitThisExpr(This expr);
    public R visitSuperExpr(Super expr);
    public R visitUnaryExpr(Unary expr);
    public R visitVariableExpr(Variable expr);
    public R visitCallExpr(Call expr);
    public R visitAnonFunExpr(AnonFun expr);
    public R visitGetExpr(Get expr);
  }
  public static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitAssignExpr(this);
    }
    public final Token name;
    public final Expr value;
  }
  public static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitBinaryExpr(this);
    }
    public final Expr left;
    public final Token operator;
    public final Expr right;
  }
  public static class Ternary extends Expr {
    Ternary(Expr left, Token op1, Expr middle, Token op2, Expr right) {
      this.left = left;
      this.op1 = op1;
      this.middle = middle;
      this.op2 = op2;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitTernaryExpr(this);
    }
    public final Expr left;
    public final Token op1;
    public final Expr middle;
    public final Token op2;
    public final Expr right;
  }
  public static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitGroupingExpr(this);
    }
    public final Expr expression;
  }
  public static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitLiteralExpr(this);
    }
    public final Object value;
  }
  public static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitLogicalExpr(this);
    }
    public final Expr left;
    public final Token operator;
    public final Expr right;
  }
  public static class Set extends Expr {
    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitSetExpr(this);
    }
    public final Expr object;
    public final Token name;
    public final Expr value;
  }
  public static class This extends Expr {
    This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitThisExpr(this);
    }
    public final Token keyword;
  }
  public static class Super extends Expr {
    Super(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitSuperExpr(this);
    }
    public final Token keyword;
    public final Token method;
  }
  public static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitUnaryExpr(this);
    }
    public final Token operator;
    public final Expr right;
  }
  public static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitVariableExpr(this);
    }
    public final Token name;
  }
  public static class Call extends Expr {
    Call(Expr calee, Token paren, List<Expr> arguments) {
      this.calee = calee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitCallExpr(this);
    }
    public final Expr calee;
    public final Token paren;
    public final List<Expr> arguments;
  }
  public static class AnonFun extends Expr {
    AnonFun(List<Token> params, List<Stmt> body) {
      this.params = params;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitAnonFunExpr(this);
    }
    public final List<Token> params;
    public final List<Stmt> body;
  }
  public static class Get extends Expr {
    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
    return visitor.visitGetExpr(this);
    }
    public final Expr object;
    public final Token name;
  }

  public abstract <R> R accept(Visitor<R> visitor);
}
