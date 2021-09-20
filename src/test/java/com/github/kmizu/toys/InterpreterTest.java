package com.github.kmizu.toys;

import org.javafp.parsecj.input.Input;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.Optional;

import static com.github.kmizu.toys.Ast.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class InterpreterTest {
    private Interpreter interpreter = new Interpreter();

    @Test
    public void testWhile() {
        // i = 0;
        // while(i < 10) {
        //   println(i);
        //   i = i + 1;
        // }
        // と同等
        Ast.Expression program = Block(
                Assignment("i", integer(0)),
                While(
                        lessThan(symbol("i"), integer(10)),
                        Block(
                                Println(symbol("i")),
                                Assignment("i", add(symbol("i"), integer(1)))
                        )
                )
        );
        interpreter.interpret(program);
        assertTrue(true);
    }

    @Test
    public void testFactorial() {
        List<TopLevel> topLevels = List.of(
                // def main() {
                //   println(fact(5));
                // }
                DefineFunction("main", List.of(), Block(
                        Println(call("fact", integer(5)))
                )),
                // def factorial(n) {
                //   if(n < 2) {
                //     1
                //   } else {
                //     n * fact(n - 1)
                //   }
                // }
                DefineFunction("fact", List.of("n"), Block(
                        If(
                                lessThan(symbol("n"), integer(2)),
                                integer(1),
                                Optional.of(
                                        (Ast.Expression)multiply(symbol("n"), call("fact", subtract(symbol("n"), integer(1))))
                                )
                        )))
        );
        int result = interpreter.callMain(new Ast.Program(topLevels));
        assertEquals(120, result);
    }

    @Test
    public void testExpression() throws Exception {
        Expression tree;
        tree = Parsers.expression().parse(Input.of("(1+2)*3")).getResult();
        assertEquals(9, interpreter.interpret(tree));
        tree = Parsers.expression().parse(Input.of("1 + (2 * 3)")).getResult();
        assertEquals(7, interpreter.interpret(tree));
    }

    @Test
    public void testAssignment() throws Exception {
        Expression tree;
        tree = Parsers.line().parse(Input.of("a = 1 + 2;")).getResult();
        int value;
        value = interpreter.interpret(tree);
        assertEquals(3, value);

        tree = Parsers.line().parse(Input.of("a = a + 1;")).getResult();
        value = interpreter.interpret(tree);
        assertEquals(4, value);
    }

    @Test
    public void testIfExpression() throws Exception {
        Expression tree;
        tree = Parsers.ifExpression().parse(Input.of("""
                if(1 >= 2) {
                   a = 1;
                } else {
                   a = 0;
                }""")).getResult();
        int value;
        value = interpreter.interpret(tree);
        assertEquals(0, value);

        tree = Parsers.ifExpression().parse(Input.of("""
                if(1 >= 2) {
                   b = 1;
                } else {
                   b = 0;
                }""")).getResult();
        value = interpreter.interpret(tree);
        assertEquals(0, value);
    }

    @Test
    public void testWhileExpression() throws Exception {
        var statements = Parsers.lines().parse(Input.of("""
                i = 0;
                while(i < 10) {
                  i = i + 1;
                }""")).getResult();
        for (var statement : statements) {
            interpreter.interpret(statement);
        }
        assertEquals(10, interpreter.getValue("i"));
    }

    @Test
    public void testFunctionDefinition() throws Exception {
        var program = Parsers.program().parse(Input. of("""
                global v = 2;
                define add2(x) {
                  x + 2;
                }
                define main() {
                  v = add2(v);
                }""")).getResult();
        int value = interpreter.callMain(program);
        assertEquals(4, value);
    }

    @Test
    public void testFactorial2() throws Exception {
        var program = Parsers.program().parse(Input.of("""
                //階乗を計算するプログラム
                define factorial(n) {
                  if(n < 2) {
                    1;
                  } else {
                    n * factorial(n - 1);
                  }
                }
                global n = 0;
                define main() {
                  n = factorial(5);
                  println(n);
                }""")).getResult();
        interpreter.callMain(program);
        assertEquals(120, interpreter.getValue("n"));
    }

    @Test
    public void testForInExpression() throws Exception {
        var statements = Parsers.lines().parse(Input.of("""
                for(i in 1 to 10) {
                  i = i + 1;
                }""")).getResult();
        for (var statement : statements) {
            interpreter.interpret(statement);
        }
        assertEquals(11, interpreter.getValue("i"));
    }

    @Test
    public void testLabelledCall() throws Exception {
        var program = Parsers.program().parse(Input.of("""
                //二乗を計算するプログラム
                define power(n) {
                  n * n;
                }
                define main() {
                  power[n = 5];
                }""")).getResult();
        var result = interpreter.callMain(program);
        assertEquals(25, result);
    }
}