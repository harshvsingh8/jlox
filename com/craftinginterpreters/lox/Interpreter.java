package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;
import static com.craftinginterpreters.lox.TokenType.*;

public class Interpreter implements Expr.Visitor<Object> {

    /* (non-Javadoc)
     * @see com.craftinginterpreters.lox.Expr.Visitor#visitBinaryExpr(com.craftinginterpreters.lox.Expr.Binary)
     */
    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = this.evaluate(expr.left);
        Object right = this.evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                return (double)left - (double)right;
            case SLASH:
                return (double)left / (double)right;
            case STAR:
                return (double)left * (double)right;
            case PLUS:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left * (double)right;
                }
                if(left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                break;
            case GREATER:
                return (double)left > (double)right;
            case LESS:
                return (double)left < (double)right;
            case GREATER_EQUAL:
                return (double)left >= (double)right;
            case LESS_EQUAL:
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
}
