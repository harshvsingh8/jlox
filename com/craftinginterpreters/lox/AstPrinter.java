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
import com.craftinginterpreters.lox.Expr.Get;
import com.craftinginterpreters.lox.Expr.Set;
import com.craftinginterpreters.lox.Expr.This;
import com.craftinginterpreters.lox.Expr.Super;

public class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return this.parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return this.parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariableExpr'");
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpr'");
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpr'");
    }

    @Override
    public String visitCallExpr(Call call) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    @Override
    public String visitAnonFunExpr(AnonFun expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAnonFunExpr'");
    }

    @Override
    public String visitGetExpr(Get expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGetExpr'");
    }

    @Override
    public String visitSetExpr(Set expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSetExpr'");
    }

    @Override
    public String visitThisExpr(This expr) {
        throw new UnsupportedOperationException("Unimplemented method 'visitThisExpr'");
    }

    @Override
    public String visitSuperExpr(Super expr) {
        throw new UnsupportedOperationException("Unimplemented method 'visitSuperExpr'");
    }
}

