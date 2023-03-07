package com.craftinginterpreters.lox;

class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
	// special constructor to avoid stack tracking
	super(null, null, false, false);
	this.value = value;
    }
}
