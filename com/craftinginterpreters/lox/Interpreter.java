package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Assign;
import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.AnonFun;
import com.craftinginterpreters.lox.Stmt.Block;
import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Var;
import com.craftinginterpreters.lox.Stmt.If;
import com.craftinginterpreters.lox.Stmt.While;
import com.craftinginterpreters.lox.Stmt.Break;
import com.craftinginterpreters.lox.Stmt.Function;

import static com.craftinginterpreters.lox.TokenType.*;

import java.util.List;
import java.util.ArrayList;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private static class LoopExitJump extends RuntimeException {}

    final Environment globals = new Environment();
    private Environment environment = globals;
    
    private int loopDepth = 0;

    public Interpreter() {
        Globals.defineNativeFunctions(globals);
    }
    
    public void interpret(Expr expression) {
        try {
            Object value = this.evaluate(expression);
            System.out.println(this.stringify(value));
        } catch (RuntimeError error)    {
            Lox.runtimeError(error);
        }
    }

    public void interpret(List<Stmt> statements) {
        try {
            for(Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error)    {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = this.evaluate(expr.left);
        Object right = this.evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                this.checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                this.checkNumberOperands(expr.operator, left, right);
                this.checkNumberNotZero(expr.operator, (double)right);   
                return (double)left / (double)right;
            case STAR:
                this.checkNumberOperands(expr.operator, left, right);   
                return (double)left * (double)right;
            case PLUS:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if(left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must two numbers or two strings.");
            case GREATER:
                this.checkNumberOperands(expr.operator, left, right);   
                return (double)left > (double)right;
            case LESS:
                this.checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case GREATER_EQUAL:
                this.checkNumberOperands(expr.operator, left, right);   
                return (double)left >= (double)right;
            case LESS_EQUAL:
                this.checkNumberOperands(expr.operator, left, right);   
                return (double)left <= (double)right;
            case EQUAL_EQUAL:
                return this.isEqual(left, right);
            case BANG_EQUAL:
                return !this.isEqual(left, right);
        }
        
        return null;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return this.evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = this.evaluate(expr.right);
        switch(expr.operator.type) {
            case MINUS:
                return -(double)right;
            case BANG:
                return !this.isTruthy(right);
        }

        return null;
    }

    // Statement 
    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        loopDepth++;

        while(isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch(LoopExitJump lej) {
                break;
            }
        }

        loopDepth--;
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if(stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        
        return null;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);

        if(expr.operator.type == TokenType.OR) {
            if(isTruthy(left)) return left;
        } else {
            if(!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitAnonFunExpr(AnonFun expr) {
        return  new LoxFunction(expr, environment);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private boolean isTruthy(Object object) {
        if( object == null) return false;
        if( object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if(a == null && b == null) return true;
        if(a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberNotZero(Token operator, double value) {
        if(value == 0) throw new RuntimeError(operator, "Division by zero error.");
    }
    
    /* (non-Javadoc)
     * @see com.craftinginterpreters.lox.Expr.Visitor#visitBinaryExpr(com.craftinginterpreters.lox.Expr.Binary)
     */
    private String stringify(Object value) {
        if(value == null) return "nil";

        if(value instanceof Double) {
            String text = value.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;
        if(stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        if(loopDepth == 0) {
            throw new RuntimeError(stmt.token, "Encountered 'break' without enclosing loop.");
        }

        throw new LoopExitJump();
    }

    @Override
    public Object visitCallExpr(Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for(Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only expr functions and classes");
        }
        
        LoxCallable function = (LoxCallable)callee;

        if(arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
                                   String.format("Expected %d arguments but got %d.",
                                                 function.arity(),
                                                 arguments.size()));
        }
        return function.call(this, arguments);
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if(stmt.value != null) {
            value = evaluate(stmt.value);
        }
        throw new Return(value);
    }
    
    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for(Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
}
