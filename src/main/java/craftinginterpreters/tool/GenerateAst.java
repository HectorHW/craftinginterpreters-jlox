package craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    //скрипт для генерации повторяющегося кода, нужного для реализации абстрактного синтаксического дерева
    //с его помощью создадим файл craftinginterpreters.lox.Expr.java
    //может быть, перепишем его на python?

    public static void main(String[] args)  throws IOException {
        if(args.length!=1){
            System.out.println("Usage: generate_ast <directory>");
            System.exit(64);
        }
        var outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Assign : Token name, Expr value",
            "Binary : Expr left, Token operator, Expr right",
            "Ternary : Expr left, Token op1, Expr middle, Token op2, Expr right",
            "Grouping : Expr expression",
            "Literal : Object value",
            "Logical : Expr left, Token operator, Expr right",
            "Set : Expr object, Token name, Expr value", // object.name = value
            "This : Token keyword",
            "Super: Token keyword, Token method",
            "Unary : Token operator, Expr right",
            "Variable : Token name",
            "Call : Expr calee, Token paren, List<Expr> arguments",
            "AnonFun : List<Token> params, List<Stmt> body",
            "Get : Expr object, Token name" // код вида objectName.name
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block : List<Stmt> statements",
            "Class : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
            "Expression : Expr expression",
            "If : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "Print : Expr expression",
            "Var : Token name, Expr initializer",
            "While : Expr condition, Stmt body",
            "For : Stmt init, Expr condition, Stmt body, Expr increment",
            "ControlStatement : Token parameter",
            "Function : Token name, List<Token> params, List<Stmt> body, boolean isStaticClassMethod",
            "Return : Token keyword, Expr value"
        ));
    }
    private static void defineAst(
        String outputDir, String baseName, List<String> types
    ) throws IOException{
        var path = outputDir + "/" + baseName + ".java";
        var writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        //классы AST
        for(String type: types){
            var className = type.split(":")[0].trim(); // Binary, Grouping etc
            var fields = type.split(":")[1].trim(); // Expr left, Token operator, Expr right
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();

    }

    private static void defineVisitor(
        PrintWriter writer, String baseName, List<String> types
    ){
        writer.println("  public interface Visitor<R> {");

        for(String type : types) {
            var typeName = type.split(":")[0].trim();
            writer.println(
                "    public R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("  }");
    }


    private static void defineType(
        PrintWriter writer, String baseName, String className, String fieldList
    ) {
        writer.println("  public static class " + className + " extends " + baseName + " {");
        writer.println("    " + className + "(" + fieldList + ") {"); //конструктор
        var fields = fieldList.split(", ");
        for(String field: fields){
            var name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");

        }
        writer.println("    }");
        writer.println();

        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println(
            "    return visitor.visit" + className + baseName + "(this);"
        );
        writer.println("    }");

        for(String field: fields){
            writer.println("    public final " + field + ";");
        }

        writer.println("  }");
    }
}
