package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.craftinginterpreters.lox.Expr.AnonFun;
import com.craftinginterpreters.lox.Expr.Assign;
import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Expr.Get;
import com.craftinginterpreters.lox.Expr.Set;
import com.craftinginterpreters.lox.Expr.This;
import com.craftinginterpreters.lox.Stmt.Block;
import com.craftinginterpreters.lox.Stmt.Break;
import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Function;
import com.craftinginterpreters.lox.Stmt.Class;
import com.craftinginterpreters.lox.Stmt.If;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Return;
import com.craftinginterpreters.lox.Stmt.Var;
import com.craftinginterpreters.lox.Stmt.While;

import static com.craftinginterpreters.lox.TokenType.*;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    private ClassType currentClass = ClassType.NONE;
    private FunctionType currentFunction = FunctionType.NONE;
    
    public Resolver(Interpreter interpreter) {
	this.interpreter = interpreter;
    }

    private enum ClassType {
        NONE,
        CLASS
    }
    
    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }

    void resolve(List<Stmt> statements) {
        for(Stmt statement : statements) {
            resolve(statement);
        }
    }

    void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr) {
        expr.accept(this);
    }
    
    @Override
    public Void visitBlockStmt(Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        declare(stmt.name);
        // define immediately as it should be valid (to support recursion).
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        
        declare(stmt.name);
        define(stmt.name);

        // Create a new environment scope and put "this" in that.
        beginScope();
        scopes.peek().put("this", true /* defined */);

        for(Stmt.Function method : stmt.methods) {
            FunctionType funcType = FunctionType.METHOD;
            if(method.name.lexeme.equals("init")) {
                funcType = FunctionType.INITIALIZER;
            }
            resolveFunction(method, funcType);
        }

        endScope();
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        declare(stmt.name);
        if(stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if(currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Cannot return form top-level code.");
        }
        if(currentFunction == FunctionType.INITIALIZER && stmt.value != null) {
            Lox.error(stmt.keyword, "Cannot return a value from an initializer.");
        }
        if(stmt.value != null) {
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        // The funtion/callee too could be an expression.
        resolve(expr.callee);

        for(Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpr(Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpr(Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitThisExpr(This expr) {
        // Check validity of the placement of "this" expression.
        if(currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class.");
        }
        
        resolveLocal(expr, expr.keyword);
        return null;
    }
    
    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        if(!scopes.isEmpty()
           && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name, "Cannot refer local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAnonFunExpr(AnonFun expr) {
        FunctionType enclosingFunctionType = currentFunction;
        currentFunction = FunctionType.FUNCTION;
        // Entering the anonymous function starts a new scope.
        beginScope();
        for(Token param : expr.params) {
            // define all params - these will be resolve/bound at the usages sites inside function.body
            declare(param);
            define(param);
        }
        resolve(expr.body);
        endScope();
        currentFunction = enclosingFunctionType;
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        // first resolve the assignment expression.
        resolve(expr.value);
        // then bind the scope of the local variable.
        resolveLocal(expr, expr.name);
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        // skip for global level declarations.
        if(scopes.isEmpty()) return;
        if(scopes.peek().containsKey(name.lexeme)) {
            Lox.error(name, "Variable re-declaration in the same scope.");
        }
        scopes.peek().put(name.lexeme, false /* not defined yet */);
    }

    private void define(Token name) {
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true /* defined now */);
    }

    // Important method: defines a local variable binding to its scope.
    private void resolveLocal(Expr expr, Token name) {
        // find name from the most inner scope
        for(int i = scopes.size() - 1; i >= 0; i--) {
            if(scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr /* variable usages node */,
                                    scopes.size() - 1 - i /* steps outside in the outer scopes */);
                return;
            }
        }
    }

    private void resolveFunction(Function function, FunctionType type) {
        FunctionType enclosingFunctionType = currentFunction;
        currentFunction = type;
        // Entering the function starts a new scope.
        beginScope();
        for(Token param : function.params) {
            // define all params - these will be resolve/bound at the usages sites inside function.body
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunctionType;
    }
}

