package com.github.kmizu.calculator;

public class Ast {
    public static BinaryExpression add(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.ADD, lhs, rhs);
    }
    public static BinaryExpression subtract(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.SUBTRACT, lhs, rhs);
    }
    public static BinaryExpression multiply(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.MULTIPLY, lhs, rhs);
    }
    public static BinaryExpression divide(Expression lhs, Expression rhs) {
        return new BinaryExpression(Operator.DIVIDE, lhs, rhs);
    }
    public static IntegerLiteral integer(int value) {
        return new IntegerLiteral(value);
    }

    sealed public interface Expression permits BinaryExpression, IntegerLiteral {}
    public static final record BinaryExpression(Operator operator, Expression lhs, Expression rhs) implements Expression {}
    public static final record IntegerLiteral(int value) implements Expression {}
}