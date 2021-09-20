package com.github.kmizu.calculator;

public class Interpreter {

    public int interpret(Ast.Expression expression) {
        if(expression instanceof Ast.BinaryExpression binaryExpression) {
            var lhs = interpret(binaryExpression.lhs());
            var rhs = interpret(binaryExpression.rhs());
            return switch(binaryExpression.operator()) {
                case ADD -> lhs + rhs;
                case SUBTRACT -> lhs - rhs;
                case MULTIPLY -> lhs * rhs;
                case DIVIDE -> lhs / rhs;
            };
        } else if (expression instanceof Ast.IntegerLiteral integerLiteral){
            return integerLiteral.value();
        } else {
            throw new RuntimeException("not reach here");
        }
    }
}
