package com.craftinginterpreters.lox;

import java.util.List;

public class Globals {

    public static void defineNativeFunctions(Environment environment) {

	environment.define("clock", new LoxCallable() {
		@Override
		public int arity() {
		    return 0;
		}

		@Override
		public Object call(Interpreter interpreter,
				   List<Object> arguments) {
		    return (double)System.currentTimeMillis();
		}

		@Override
		public String toString() {
		    return "<native fn>";
		}
	    });

	// template body to define new global native methods.
	environment.define("_func_name_", new LoxCallable() {
		@Override
		public int arity() {
		    throw new UnsupportedOperationException("Unimplemented");
		}

		@Override
		public Object call(Interpreter interpreter,
				   List<Object> arguments) {
		    throw new UnsupportedOperationException("Unimplemented");
		}

		@Override
		public String toString() {
		    return "<native fn>";
		}
	    });
    }
}

