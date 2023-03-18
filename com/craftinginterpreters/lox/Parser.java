package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    public static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private static final int maxArgListLength = 255;
    private int current = 0;
    private boolean repl;

    Parser(List<Token> tokens, boolean repl) {
        this.tokens = tokens;
        this.repl = repl;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if(match(CLASS)) return classDeclaration();
            if(match(FUN)) return funDeclaration("function");
            if(match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if(match(IF)) return ifStatement();
        if(match(PRINT)) return printStatement();
        if(match(RETURN)) return returnStatement();
        if(match(WHILE)) return whileStatement();
        if(match(FOR)) return forStatement();
        if(match(BREAK)) return breakStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if(!repl) {
            consume(SEMICOLON, "Expect ';' after expression.");
            return new Stmt.Expression(expr);
        } else {
            // Allow once 
            repl = false;
            return new Stmt.Print(expr);
        }
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt breakStatement() {
        consume(SEMICOLON, "Expect ';' after break.");
        return new Stmt.Break(previous());
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }
    
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;

        if(match(SEMICOLON)) {
            initializer = null;
        } else if(match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;

        // if there is no immediate semicolon, then parse the condition statement.
        if(!check(SEMICOLON)) {
            condition = expression();
        }

        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;

        // if there is no immediate semicolon, then parse the increment/last clause.
        if(!check(SEMICOLON)) {
            increment = expression();
        }

        consume(RIGHT_PAREN, "Expect ')' after for clause.");

        Stmt forBody = statement();

        // Fuse increment and forBody.
        if(increment != null) {
            forBody = new Stmt.Block(
                                     Arrays.asList(
                                                   forBody,
                                                   new Stmt.Expression(increment)));
        }

        // Normalize condition expression.
        if(condition == null) {
            condition = new Expr.Literal(true);
        }

        Stmt whileBody = new Stmt.While(condition, forBody);
        
        if(initializer != null) {
            return new Stmt.Block(
                                  Arrays.asList(
                                                initializer,
                                                whileBody));
        } else {
            return whileBody;
        }
    }
    
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if(match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<Stmt>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block statement");
        return statements;
    }
    
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt.Function funDeclaration(String kind) {
        Token name = consume(IDENTIFIER, String.format("Expect %s name.", kind));
        consume(LEFT_PAREN, String.format("Expect '(' after %s name.", kind));
        List<Token> parameters = new ArrayList<>();
        if(!check(RIGHT_PAREN)) {
            do {
                if(parameters.size() >= maxArgListLength) {
                    error(peek(), String.format("Cannot have more than %d parameters.", maxArgListLength));
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, String.format("Expect '{' before %s body.", kind));
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(funDeclaration("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, methods);
    }
    
    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if(match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } if(expr instanceof Expr.Get) {
                // set's l-value is parsed as Expr.Get - we will repurpose.
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while(match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while(match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }
        
    private Expr equality() {
        Expr expr = this.comparison();
        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while(match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while(match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while(true) {
            if(match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } if(match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect a propertry name after '.'.");
                expr = new Expr.Get(expr, name);
            }else {
                break;
            }
        }

        return expr;
    }
    
    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);
        
        if(match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if(match(FUN)) {
            return prepareAnonFun();
        }

        if(match(THIS)) {
            return new Expr.This(previous());
        }
        
        if(match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        
        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Unexpected expression.");
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PAREN)) {
            // if there are any arguments.
            do {
                if(arguments.size() >= maxArgListLength) {
                    error(peek(), String.format("Can't have more than %d arguments", maxArgListLength));
                }
                arguments.add(expression());
            } while(match(COMMA));
        }

        Token paran = consume(RIGHT_PAREN, "Expect ')' after arguments");
        return new Expr.Call(callee, paran, arguments);
    }

    private Expr prepareAnonFun() {
        List<Token> parameters = new ArrayList<>();
        consume(LEFT_PAREN, "Expect '(' after fun.");
        if(!check(RIGHT_PAREN)) {
            do {
                if(parameters.size() >= maxArgListLength) {
                    error(peek(), String.format("Cannot have more than %d parameters.", maxArgListLength));
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, String.format("Expect '{' before function body."));
        List<Stmt> body = block();

        // TODO - internal name can be added (auto-incremented ones) to anonymous function.
        return new Expr.AnonFun(parameters, body);
    }
    
    // Match one or more (any) tokens at the current token stream cursor.
    private boolean match(TokenType... types) {
        for(TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }
    
    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType token, String message) {
        if(check(token)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    // TODO - this is to be used later.
    private void synchronize() {
        advance();
        while(!isAtEnd()) {
            if(previous().type == TokenType.SEMICOLON) return;
            switch(peek().type) {
                case CLASS:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }
            advance();
        }
    }
}
