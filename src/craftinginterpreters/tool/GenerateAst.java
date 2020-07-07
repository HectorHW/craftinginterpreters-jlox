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
            "Binary : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal : Object value",
            "Unary : Token operator, Expr right"
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
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        //классы AST
        for(String type: types){
            var className = type.split(":")[0].trim(); // Binary, Grouping etc
            var fields = type.split(":")[1].trim(); // Expr left, Token operator, Expr right
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();

    }

    private static void defineVisitor(
        PrintWriter writer, String baseName, List<String> types
    ){
        writer.println("  interface Visitor<R> {");

        for(String type : types) {
            var typeName = type.split(":")[0].trim();
            writer.println(
                "    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("  }");
    }


    private static void defineType(
        PrintWriter writer, String baseName, String className, String fieldList
    ) {
        writer.println("  static class " + className + " extends " + baseName + " {");
        writer.println("    " + className + "(" + fieldList + ") {"); //конструктор
        var fields = fieldList.split(", ");
        for(String field: fields){
            var name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");

        }
        writer.println("    }");
        writer.println();

        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println(
            "    return visitor.visit" + className + baseName + "(this);"
        );
        writer.println("    }");

        for(String field: fields){
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }
}
