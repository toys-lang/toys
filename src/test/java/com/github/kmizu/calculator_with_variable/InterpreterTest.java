package com.github.kmizu.calculator_with_variable;

import org.apache.commons.math3.analysis.function.Exp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.github.kmizu.calculator_with_variable.Ast.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class InterpreterTest {
    private Interpreter interpreter = new Interpreter();

    @Test
    public void testAssignmentShouldWork() {
        Expression e = assignment("a", add(integer(10), integer(10)));
        assertEquals(20 ,interpreter.interpret(e));
        assertEquals(20, interpreter.environment.get("a").intValue());
    }

    @Test
    public void testIdentifierAfterAssignmentShouldWork() {
        Expression e1 = assignment("a", add(integer(10), integer(10)));
        assertEquals(20 ,interpreter.interpret(e1));
        Expression e2 = add(identifier("a"), integer(10));
        assertEquals(30 ,interpreter.interpret(e2));
    }

    @Test
    public void testIncrementShouldWork() {
        Expression e1 = assignment("a", integer(10));
        assertEquals(10 ,interpreter.interpret(e1));
        Expression e2 = assignment("a", add(identifier("a"), integer(1)));
        assertEquals(11 ,interpreter.interpret(e2));
        assertEquals(11, interpreter.environment.get("a").intValue());
    }
}