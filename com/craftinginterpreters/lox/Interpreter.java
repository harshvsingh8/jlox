package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;
import static com.craftinginterpreters.lox.TokenType.*;

public class Interpreter implements Expr.Visitor<Object> {

    public void interpret(Expr expression) {
        try {
            Object value = this.evaluate(expression);
            System.out.println(this.stringify(value));
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
    
    private Object evaluate(Expr expr) {
        return expr.accept(this);
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
}
