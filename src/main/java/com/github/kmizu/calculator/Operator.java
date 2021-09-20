package com.github.kmizu.calculator;

public enum Operator {
    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("*");
    private String name;
    public String getName() {
        return name;
    }
    Operator(String name) {
        this.name = name;
    }
}
