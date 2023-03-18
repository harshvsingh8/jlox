package com.craftinginterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final String name;
    private final List<Token> params;
    private final List<Stmt> body;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
	this.closure = closure;
	this.name = declaration.name.lexeme;
	this.params = declaration.params;
	this.body = declaration.body;
	this.isInitializer = isInitializer;
    }

    LoxFunction(Expr.AnonFun definition, Environment closure) {
	this.closure = closure;
	this.name = "__AnonFunc__";
	this.params = definition.params;
	this.body = definition.body;
	this.isInitializer = false;
    }

    LoxFunction(String name, List<Token> params, List<Stmt> body, Environment closure, boolean isInitializer) {
	this.name = name;
	this.params = params;
	this.body = body;
	this.closure = closure;
	this.isInitializer = isInitializer;
    }
    
    LoxFunction bind(LoxInstance instance) {
	// introduce a new closure enviroment for local (this) defintion.
	Environment environment = new Environment(closure);
	environment.define("this", instance);
	return  new LoxFunction(this.name, this.params, this.body, environment, this.isInitializer);
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
	    // special case to handle explicit return from init() method.
	    if(isInitializer) return closure.getAt(0, "this");
	    return returnValue.value;
	}

	// special case to return "this" when init is called.
	if(isInitializer) return closure.getAt(0, "this");

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
