package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;

    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if(args.length > 1) {
            System.out.println("Usages: jlox [script]");
            System.exit((64));
        } else if(args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
            // runAstPrinter();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null || line.isEmpty()) break;
            run(line);
        }
    }

    // Test Ast Printer to check that the auto-gen ast classes are set correctly.
    private static void runAstPrinter() {
        Expr expression = new Expr.Binary(
            new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(new Expr.Literal(42.42)));
        System.out.println(new AstPrinter().print(expression));
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        
        // Expr  expr = parser.parse();
        List<Stmt> statements = parser.parse();
        if(hadError) return;
        interpreter.interpret(statements);
        
        // Prints the AST tree.
        // System.out.println(new AstPrinter().print(expr));
        // interpreter.interpret(expr);
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if(token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        err("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }
    
    private static void out(String msg) {
        System.out.println(msg);
    }

    private static void err(String msg) {
        System.err.println(msg);
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
