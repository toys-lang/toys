package com.github.kmizu.toys;

import org.javafp.data.IList;
import org.javafp.data.Unit;
import org.javafp.parsecj.*;

import java.util.List;
import java.util.function.BinaryOperator;

import static org.javafp.parsecj.Text.*;
import static com.github.kmizu.toys.Ast.*;

public class Parsers {
    public static final Parser<Character, Unit> SPACING = wspace.map(__1 -> Unit.unit).or(regex("(?m)//.*$").map(__1 -> Unit.unit));
    public static final Parser<Character, Unit> SPACINGS = SPACING.many().map(__1 -> Unit.unit);
    public static final Parser<Character, Unit> IF = string("if").then(SPACINGS);
    public static final Parser<Character, Unit> ELSE = string("else").then(SPACINGS);
    public static final Parser<Character, Unit> WHILE = string("while").then(SPACINGS);
    public static final Parser<Character, Unit> PLUS = string("+").then(SPACINGS);
    public static final Parser<Character, Unit> MINUS = string("-").then(SPACINGS);
    public static final Parser<Character, Unit> ASTER = string("*").then(SPACINGS);
    public static final Parser<Character, Unit> SLASH = string("/").then(SPACINGS);
    public static final Parser<Character, Unit> LT = string("<").then(SPACINGS);
    public static final Parser<Character, Unit> LT_EQ = string("<=").then(SPACINGS);
    public static final Parser<Character, Unit> GT = string(">").then(SPACINGS);
    public static final Parser<Character, Unit> GT_EQ = string(">=").then(SPACINGS);
    public static final Parser<Character, Unit> EQEQ = string("==").then(SPACINGS);
    public static final Parser<Character, Unit> NOT_EQ = string("!=").then(SPACINGS);
    public static final Parser<Character, Unit> EQ = string("=").then(SPACINGS);
    public static final Parser<Character, Unit> GLOBAL = string("global").then(SPACINGS);
    public static final Parser<Character, Unit> DEFINE = string("define").then(SPACINGS);
    public static final Parser<Character, Unit> RETURN = string("return").then(SPACINGS);
    public static final Parser<Character, Unit> PRINTLN = string("println").then(SPACINGS);
    public static final Parser<Character, Unit> TRUE = string("true").then(SPACINGS);
    public static final Parser<Character, Unit> FALSE = string("false").then(SPACINGS);
    public static final Parser<Character, Unit> COMMA = string(",").then(SPACINGS);
    public static final Parser<Character, Unit> LPAREN = string("(").then(SPACINGS);
    public static final Parser<Character, Unit> RPAREN = string(")").then(SPACINGS);
    public static final Parser<Character, Unit> LBRACE = string("{").then(SPACINGS);
    public static final Parser<Character, Unit> RBRACE = string("}").then(SPACINGS);
    public static final Parser<Character, Unit> LBRACKET = string("[").then(SPACINGS);
    public static final Parser<Character, Unit> RBRACKET = string("]").then(SPACINGS);
    public static final Parser<Character, Unit> SEMI_COLON = string(";").then(SPACINGS);
    public static final Parser<Character, String> IDENT = regex("[a-zA-Z_][a-zA-Z0-9_]*").bind(name -> SPACINGS.map(__ -> name));

    public static final Parser<Character, Unit> FOR = string("for").then(SPACINGS);
    public static final Parser<Character, Unit> IN = string("in").then(SPACINGS);
    public static final Parser<Character, Unit> TO = string("to").then(SPACINGS);

    public static Parser<Character, Ast.IntegerLiteral> integer = intr.map(Ast::integer).bind(v -> SPACINGS.map(__ -> v));

    public static Parser<Character, Ast.Program> program () {
        return SPACINGS.bind(_1 ->
                topLevelDefinition().many().map(IList::toList).map(Program::new));
    }

    public static Parser<Character, List<Ast.Expression>> lines() {
        return line().many1().bind(s -> Combinators.<Character>eof().map(__ -> s.toList()));
    }

    public static Parser<Character, Ast.TopLevel> topLevelDefinition() {
        return globalVariableDefinition().map(g -> (Ast.TopLevel)g).or(functionDefinition().map(f -> (Ast.TopLevel)f));
    }

    public static Parser<Character, Ast.FunctionDefinition> functionDefinition() {
        var defName= DEFINE.then(IDENT);
        var defArgs= IDENT.sepBy(COMMA).between(LPAREN, RPAREN);
        return defName.bind(name ->
                defArgs.bind(args ->
                        blockExpression().map(body -> new Ast.FunctionDefinition(name, args.toList(), body))
                )
        );
    }

    public static Parser<Character, Ast.GlobalVariableDefinition> globalVariableDefinition() {
        var defGlobal = GLOBAL.then(IDENT);
        var defInitializer = EQ.then(expression());
        return defGlobal.bind(name ->
                defInitializer.bind(expression ->
                        SEMI_COLON.map(_1 -> new Ast.GlobalVariableDefinition(name, expression))));
    }

    public static Parser<Character, Ast.Expression> line() {
        return println().or(whileExpression()).or(ifExpression()).or(forInExpression()).or(assignment()).or(expressionLine()).or(blockExpression());
    }

    public static Parser<Character, Ast.Expression> println() {
        return PRINTLN.bind(_1 ->
                expression().between(LPAREN, RPAREN).bind(param ->
                        SEMI_COLON.map(_2 -> (Ast.Expression)new Println(param)))
        ).attempt();
    }

    public static Parser<Character, Ast.Expression> ifExpression() {
        var condition = IF.then(expression().between(LPAREN, RPAREN));
        return condition.bind(c ->
                line().bind(thenClause ->
                        ELSE.then(line()).optionalOpt().map(elseClauseOpt -> (Ast.Expression)new IfExpression(c, thenClause, elseClauseOpt)))).attempt();
    }

    public static Parser<Character, Ast.Expression> whileExpression() {
        var condition = WHILE.then(expression().between(LPAREN, RPAREN));
        return condition.bind(c ->
                line().map(body -> (Ast.Expression)new WhileExpression(c, body))).attempt();
    }

    public static Parser<Character, Ast.Expression> forInExpression() {
        return FOR.then(
                LPAREN.then(IDENT).bind(name ->
                    IN.then(expression()).bind(from ->
                            TO.then(expression()).bind(to ->
                                    RPAREN.then(line()).map(body ->
                                            (Expression) Block(
                                                    Assignment(name, from),
                                                    While(
                                                            lessThan(symbol(name), to),
                                                            Block(
                                                                    body,
                                                                    Assignment(name, add(symbol(name), integer(1)))
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
                )
        ).attempt();
    }

    public static Parser<Character, BlockExpression> blockExpression() {
        return LBRACE.bind(__ -> line().many()).bind(expressions-> RBRACE.map(__ -> new BlockExpression(expressions.toList())));
    }

    public static Parser<Character, Ast.Expression> assignment() {
        return IDENT.bind(name ->
                EQ.then(expression().bind(e -> SEMI_COLON.map(__ -> (Ast.Expression)new Ast.Assignment(name, e))))
        ).attempt();
    }

    public static Parser<Character, Ast.Expression> expressionLine() {
        return expression().bind(e -> SEMI_COLON.map(__ -> e)).attempt();
    }

    public static Parser<Character, Ast.Expression> expression() {
        return comparative();
    };

    public static Parser<Character, Ast.Expression> comparative() {
        Parser<Character, BinaryOperator<Expression>> lt = LT.attempt().map(op -> Ast::lessThan);
        Parser<Character, BinaryOperator<Expression>> gt = GT.attempt().map(op -> Ast::greaterThan);
        Parser<Character, BinaryOperator<Expression>> lte = LT_EQ.attempt().map(op -> Ast::lessOrEqual);
        Parser<Character, BinaryOperator<Expression>> gte = GT_EQ.attempt().map(op -> Ast::greaterOrEqual);
        Parser<Character, BinaryOperator<Expression>> eq = EQEQ.attempt().map(op -> Ast::equalEqual);
        Parser<Character, BinaryOperator<Expression>> neq = NOT_EQ.attempt().map(op -> Ast::equalEqual);
        return additive().chainl1(lte.or(gte).or(neq).or(lt).or(gt).or(eq));
    }

    public static Parser<Character, Ast.Expression> additive() {
        Parser<Character, BinaryOperator<Expression>> add = PLUS.map(op -> Ast::add);
        Parser<Character, BinaryOperator<Expression>> sub = MINUS.map(op -> Ast::subtract);
        return multitive().chainl1(add.or(sub));
    };

    public static Parser<Character, Ast.Expression> multitive() {
        Parser<Character, BinaryOperator<Expression>> mul = ASTER.map(op -> Ast::multiply);
        Parser<Character, BinaryOperator<Expression>> div = SLASH.map(op -> Ast::divide);
        return primary().chainl1(mul.or(div));
    };

    public static Parser<Character, Ast.Expression> primary() {
        return LPAREN.bind(_1 ->
                expression().bind(v ->
                        RPAREN.map(_2 -> v))).or(integer).or(functionCall()).or(labelledCall()).or(arrayLiteral()).or(boolLiteral()).or(identifier());
    };

    public static Parser<Character, Ast.FunctionCall> functionCall() {
        return IDENT.bind(name ->
                expression().sepBy(COMMA).between(LPAREN, RPAREN).map(params -> new Ast.FunctionCall(name, params.toList()))
        ).attempt();
    }

    public static Parser<Character, Ast.LabelledCall> labelledCall() {
        return IDENT.bind(name ->
                IDENT.bind(label -> EQ.then(expression()).map(param -> new LabelledParameter(label, param)))
                     .sepBy(COMMA).between(LBRACKET, RBRACKET).map(params -> new Ast.LabelledCall(name, params.toList()))
        ).attempt();
    }

    public static Parser<Character, Identifier> identifier() {
        return IDENT.map(Identifier::new);
    }

    public static Parser<Character, Ast.ArrayLiteral> arrayLiteral() {
        return LBRACKET.bind(__1 ->
                expression().sepBy(COMMA).bind(params ->
                        RBRACKET.map(__2 -> new ArrayLiteral(params.toList()))
                )
        );
    }

    public static Parser<Character, Ast.BoolLiteral> boolLiteral() {
        return TRUE.map(__ -> new BoolLiteral(true)).or(FALSE.map(__ -> new BoolLiteral(false)));
    }
}
