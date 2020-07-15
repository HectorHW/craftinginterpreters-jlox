package craftinginterpreters.lox;

import craftinginterpreters.lox.checkers.BaseChecker;
import craftinginterpreters.lox.checkers.CheckExecutor;
import craftinginterpreters.lox.checkers.PreResolveCheckExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static boolean reportWarning = false;
    public static void main(String[] args) throws IOException {
        //у нас есть два варианта использования интерпретатора:
        //исполнение файла или исполнение команд из стандартного ввода
        if(args.length>2){
            System.out.println("usage: jlox [script]");
            System.exit(64);
        }else if(args.length==1){
            runFile(args[0]);
        }else if(args.length==2){
            if(args[0].startsWith("-")){
                if(args[0].contains("w")) reportWarning = true;
                runFile(args[1]);
            }else{
                System.out.println("usage: jlox [-w] script | jlox");
                System.exit(64);
            }
        } else{
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
        if(hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        //режим REPL для исполнения команд, поступающих на ввод
        var in = new InputStreamReader(System.in);
        var reader = new BufferedReader(in);
        while(true){
            System.out.print("> ");
            var line = reader.readLine();
            if(line==null) break; //выход по Ctrl + D
            if(line.endsWith(";")){
                run(line);
            }else{
                runAsREPLExpression(line);
            }

            hadError = false; //мы не хотим, чтобы ошибки завершали программу REPL
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        //System.out.println(tokens);

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if(hadError) return; //в случае ошибки выходим так как дерева тогда у нас нет

        var preresolve_check = new PreResolveCheckExecutor();
        preresolve_check.check(statements);
        if(hadError) return;

        var resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        if(hadError) return;

        var checker = new CheckExecutor();
        checker.check(statements);
        if(hadError) return;

        //System.out.println(new AstPrinter().print(expression));
        interpreter.interpret(statements);

    }

    private static void runAsREPLExpression(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        //System.out.println(tokens);

        Parser parser = new Parser(tokens);
        try{
            Expr ex = parser.parseAsExpression();
            if(hadError) return; //в случае ошибки выходим так как дерева тогда у нас нет
            String result = interpreter.interpret(ex);
            System.out.println(result);
        }catch (Parser.ParseError ignored){

        }catch (RuntimeError e){
            System.out.println(e.getMessage());
        }

    }


    //сообщения об ошибках
    public static void error(int line, String message){
        report(line, "", message);
    }

    public static void error(Token token, String message){
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

    static void runtimeError(RuntimeError error){
        System.out.printf("%s\n[line %d]\n", error.getMessage(), error.token.line);
        hadRuntimeError = true;
    }

    static void warning(Token token, String message){
        if(!reportWarning) return;
        if(token.type == TokenType.EOF){
            System.out.printf("at the end: warning: %s\n", message);
        }else{
            System.out.printf("[line %d] warning: %s\n", token.line, message);
        }
    }

}
