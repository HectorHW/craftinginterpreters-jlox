package craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        //у нас есть два варианта использования интерпретатора:
        //исполнение файла или исполнение команд из стандартного ввода
        if(args.length>1){
            System.out.println("usage: jlox [script]");
            System.exit(64);
        }else if(args.length==1){
            runFile(args[0]);
        }else{
            runPrompt();
        }

    }

    private static void runFile(String filename) throws IOException {
        //благодаря тому, что ЭВМ стали маленькими и мощными, можно целиком прочитать
        // файл исходного кода и обработать его
        byte[] bytes = Files.readAllBytes(Paths.get(filename));
        run(new String(bytes, Charset.defaultCharset()));
        //проверим на наличие ошибок, при их возникновении прекратим исполнение
        if(hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        //режим REPL для исполнения команд, поступающих на ввод
        var in = new InputStreamReader(System.in);
        var reader = new BufferedReader(in);
        while(true){
            System.out.print("> ");
            var line = reader.readLine();
            if(line==null) break; //выход по Ctrl + D
            run(line);
            hadError = false; //мы не хотим, чтобы ошибки завершали программу REPL
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        System.out.println(tokens);

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if(hadError) return; //в случае ошибки выходим так как дерева тогда у нас нет

        System.out.println(new AstPrinter().print(expression));
    }


    //сообщения об ошибках
    static void error(int line, String message){
        report(line, "", message);
    }

    static void error(Token token, String message){
        if(token.type == TokenType.EOF){
            report(token.line, " at end", message);
        }else{
            report(token.line, " at '"+token.lexeme+"'", message);
        }
    }

    private static void report(int line, String where, String message){
        System.out.printf("[line %d] Error %s: %s\n", line, where, message);
        hadError = true;
    }

}
