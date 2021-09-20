package com.github.kmizu.calculator;

import com.github.kmizu.calculator.Ast;
import org.javafp.data.IList;
import org.javafp.data.Unit;
import org.javafp.parsecj.Combinators;
import org.javafp.parsecj.Parser;

import java.util.List;
import java.util.function.BinaryOperator;

import static com.github.kmizu.calculator.Ast.*;
import static org.javafp.parsecj.Text.*;

public class Parsers {
    public static final Parser<Character, Unit> SPACING = wspace.map(__1 -> Unit.unit).or(regex("(?m)//.*$").map(__1 -> Unit.unit));
    public static final Parser<Character, Unit> SPACINGS = SPACING.many().map(__1 -> Unit.unit);
    public static final Parser<Character, Unit> PLUS = string("+").then(SPACINGS);
    public static final Parser<Character, Unit> MINUS = string("-").then(SPACINGS);
    public static final Parser<Character, Unit> ASTER = string("*").then(SPACINGS);
    public static final Parser<Character, Unit> SLASH = string("/").then(SPACINGS);
    public static final Parser<Character, Unit> COMMA = string(",").then(SPACINGS);
    public static final Parser<Character, Unit> LPAREN = string("(").then(SPACINGS);
    public static final Parser<Character, Unit> RPAREN = string(")").then(SPACINGS);
    public static final Parser<Character, Unit> LBRACE = string("{").then(SPACINGS);
    public static final Parser<Character, Unit> RBRACE = string("}").then(SPACINGS);
    public static final Parser<Character, String> IDENT = regex("[a-zA-Z_][a-zA-Z0-9_]*").bind(name -> SPACINGS.map(__ -> name));

    public static Parser<Character, IntegerLiteral> integer =
        intr.map(Ast::integer).bind(v -> SPACINGS.map(__ -> v));

    // 式の「一行」
    public static Parser<Character, Expression>
        line() {
        return expression();
    }

    // expression <- additive;
    public static Parser<Character, Expression>
        expression() {
        return additive();
    };

    // additive <- multitive
    //   ('+' multitive  / '-' multitive)*;
    public static Parser<Character, Expression>
        additive() {
        Parser<Character, BinaryOperator<Expression>>
                add = PLUS.map(op -> Ast::add);
        Parser<Character, BinaryOperator<Expression>>
                sub = MINUS.map(op ->Ast::subtract);
        return multitive().chainl1(add.or(sub));
    }

    // multitive <- primary
    //   ('+' primary / '-' primary)*;
    public static Parser<Character, Expression>
        multitive() {
        Parser<Character, BinaryOperator<Expression>>
            mul = ASTER.map(op -> Ast::multiply);
        Parser<Character, BinaryOperator<Expression>>
            div = SLASH.map(op -> Ast::divide);
        return primary().chainl1(mul.or(div));
    };

    // primary <- '(' expression ')' / integer;
    public static Parser<Character, Expression>
        primary() {
        return LPAREN.bind(_1 ->
            expression().bind(v ->
                RPAREN.map(_2 -> v)
            )
        ).or(integer);
    };
}