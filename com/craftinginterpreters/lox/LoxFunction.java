package com.craftinginterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final String name;
    private final List<Token> params;
    private final List<Stmt> body;
    
    private final Environment closure;

    LoxFunction(Stmt.Function declaration, Environment closure) {
	this.closure = closure;
	this.name = declaration.name.lexeme;
	this.params = declaration.params;
	this.body = declaration.body;
    }

    LoxFunction(Expr.AnonFun definition, Environment closure) {
	this.closure = closure;
	this.name = "__AnonFunc__";
	this.params = definition.params;
	this.body = definition.body;
    }
    
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
	Environment environment = new Environment(closure);
	for(int i = 0; i < params.size(); i++) {
	    environment.define(params.get(i).lexeme, arguments.get(i));
	}
	try {
	    interpreter.executeBlock(body, environment);
	} catch(Return returnValue) {
	    return returnValue.value;
	}
	return null;
    }
    
    @Override
    public int arity() {
	return params.size();
    }

    @Override
    public String toString() {
	return String.format("<Fn %s>", name);
    }
}
