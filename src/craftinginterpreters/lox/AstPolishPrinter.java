package craftinginterpreters.lox;

public class AstPolishPrinter implements Expr.Visitor<String>{
    //принтер для AST, производящий обратную польскую запись
    // 1 + 2 * 3 ==> 1 2 3 * +
    String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this)+" "+expr.right.accept(this) +" "+ expr.operator.lexeme;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value==null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.right.accept(this) + " " + expr.operator.lexeme;
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
        System.out.println(new AstPolishPrinter().print(expr));
    }
}
