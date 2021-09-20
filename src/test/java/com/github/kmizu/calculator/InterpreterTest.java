package com.github.kmizu.calculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
import static com.github.kmizu.calculator.Ast.*;

@RunWith(JUnit4.class)
public class InterpreterTest {
    private Interpreter interpreter = new Interpreter();

    @Test
    public void test10Plus20ShouldWork() {
        Ast.Expression e = add(integer(10), integer(20));
        assertEquals(30, interpreter.interpret(e));
    }

    @Test
    public void test10Plus0ShouldWork() {
        Ast.Expression e = add(integer(10), integer(0));
        assertEquals(10, interpreter.interpret(e));
    }

    @Test
    public void test0Plus10ShouldWork() {
        Ast.Expression e = add(integer(0), integer(10));
        assertEquals(10, interpreter.interpret(e));
    }

    @Test
    public void test10Minus20ShouldWork() {
        Ast.Expression e = subtract(integer(10), integer(20));
        assertEquals(-10, interpreter.interpret(e));
    }

    @Test
    public void test10Minus0ShouldWork() {
        Ast.Expression e = subtract(integer(10), integer(0));
        assertEquals(10, interpreter.interpret(e));
    }

    @Test
    public void test0Minus10ShouldWork() {
        Ast.Expression e = subtract(integer(0), integer(10));
        assertEquals(-10, interpreter.interpret(e));
    }

    @Test
    public void test10Multiply20ShouldWork() {
        Ast.Expression e = multiply(integer(10), integer(20));
        assertEquals(200, interpreter.interpret(e));
    }

    @Test
    public void test10Multiply0ShouldWork() {
        Ast.Expression e = multiply(integer(10), integer(0));
        assertEquals(0, interpreter.interpret(e));
    }

    @Test
    public void test0Multiply10ShouldWork() {
        Ast.Expression e = multiply(integer(0), integer(10));
        assertEquals(0, interpreter.interpret(e));
    }

    @Test
    public void test10Divide20ShouldWork() {
        Ast.Expression e = divide(integer(10), integer(20));
        assertEquals(0, interpreter.interpret(e));
    }

    @Test
    public void test20Divide10ShouldWork() {
        Ast.Expression e = divide(integer(20), integer(10));
        assertEquals(2, interpreter.interpret(e));
    }

    @Test
    public void test0Divide10ShouldWork() {
        Ast.Expression e = divide(integer(0), integer(10));
        assertEquals(0, interpreter.interpret(e));
    }

    @Test(expected = ArithmeticException.class)
    public void test10Divide0ShouldNotWork() {
        Ast.Expression e = divide(integer(10), integer(0));
        interpreter.interpret(e);
    }
}