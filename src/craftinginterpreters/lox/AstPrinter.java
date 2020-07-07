package craftinginterpreters.lox;

public class AstPrinter implements Expr.Visitor<String>{
//принтер для AST, производящий записи вида
    // 1 + 2 * 3 ==> (+ 1 (* 2 3))
    String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value==null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs){
        var builder = new StringBuilder();
        builder.append("(").append(name);
        for(Expr expr : exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }


    public static void main(String[] args) {
        // 1 + 2 * 3
        var expr = new Expr.Binary(
            new Expr.Literal(1),
            new Token(TokenType.PLUS, "+", null, 1),
            new Expr.Binary(
                new Expr.Literal(2),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Literal(3)
            )

        );
        System.out.println(new AstPrinter().print(expr));
    }
}
