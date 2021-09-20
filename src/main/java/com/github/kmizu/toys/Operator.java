package com.github.kmizu.toys;

public enum Operator {
    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("*"),
    LESS_THAN("<"), LESS_OR_EQUAL("<="), GREATER_THAN(">"), GREATER_OR_EQUAL(">="), EQUAL_EQUAL("=="), NOT_EQUAL("!=");
    private String name;
    public String getName() {
        return name;
    }
    Operator(String name) {
        this.name = name;
    }
}
