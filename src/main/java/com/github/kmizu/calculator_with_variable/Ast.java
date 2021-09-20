package com.github.kmizu.calculator_with_variable;

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
    public static Identifier identifier(String name) { return new Identifier(name); }
    public static Assignment assignment(String name, Expression expression) { return new Assignment(name, expression); }

    sealed public interface Expression permits BinaryExpression, Assignment, IntegerLiteral, Identifier {}
    public static final record BinaryExpression(Operator operator, Expression lhs, Expression rhs) implements Expression {}
    public static final record Assignment(String name, Expression expression) implements Expression {}
    public static final record IntegerLiteral(int value) implements Expression {}
    public static final record Identifier(String name) implements Expression {}
}