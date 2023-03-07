package com.craftinginterpreters.lox;

import java.util.List;

// type representing something which can called.
interface LoxCallable {
    // Call implementation
    Object call(Interpreter interpreter, List<Object> arguments);

    // Gives number of arguments.
    int arity();
}

