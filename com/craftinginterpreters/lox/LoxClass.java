package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {
    final String name;
    final LoxClass superClass;
    final Map<String, LoxFunction> methods;

    LoxClass(String name,
             LoxClass superClass,
             Map<String, LoxFunction> methods) {
	this.name = name;
        this.superClass = superClass;
        this.methods = methods;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if(initializer != null) {
            // binds and call the init (constructor) immediately.
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if(initializer != null) {
            return initializer.arity();
        }
        return 0;
    }

    LoxFunction findMethod(String name) {
        if(methods.containsKey(name)) {
            return methods.get(name);
        }

        if(superClass != null) {
            return superClass.findMethod(name);
        }
        
        return null;
    }
                
    @Override
    public String toString() {
	return "class<" + name + ">";
    }
}
