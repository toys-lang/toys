package com.github.kmizu.toys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static com.github.kmizu.toys.Values.*;

public class Interpreter {
    private Ast.Environment variableEnvironment;
    private final Map<String, Ast.FunctionDefinition> functionEnvironment;

    public Interpreter() {
        this.variableEnvironment = newEnvironment(Optional.empty());
        this.functionEnvironment = new HashMap<>();
    }

    public void reset() {
        this.variableEnvironment = newEnvironment(Optional.empty());
        this.functionEnvironment.clear();
    }

    private static Ast.Environment newEnvironment(Optional<Ast.Environment> next) {
        return new Ast.Environment(new HashMap<>(), next);
    }

    public Value getValue(String name) {
        return variableEnvironment.bindings().get(name);
    }

    public Value interpret(Ast.Expression expression) {
        if(expression instanceof Ast.BinaryExpression binaryExpression) {
            var lhs = interpret(binaryExpression.lhs()).asInt().value();
            var rhs = interpret(binaryExpression.rhs()).asInt().value();
            return switch(binaryExpression.operator()) {
                case ADD -> wrap(lhs + rhs);
                case SUBTRACT -> wrap(lhs - rhs);
                case MULTIPLY -> wrap(lhs * rhs);
                case DIVIDE -> wrap(lhs / rhs);
                case LESS_THAN -> wrap(lhs < rhs);
                case LESS_OR_EQUAL -> wrap(lhs <= rhs);
                case GREATER_THAN -> wrap(lhs > rhs);
                case GREATER_OR_EQUAL -> wrap(lhs >= rhs);
                case EQUAL_EQUAL -> wrap(lhs == rhs);
                case NOT_EQUAL -> wrap(lhs != rhs);
            };
        } else if (expression instanceof Ast.IntegerLiteral integerLiteral){
            return wrap(integerLiteral.value());
        } else if (expression instanceof Ast.Identifier identifier) {
            var bindingsOpt = variableEnvironment.findBinding(identifier.name());
            return bindingsOpt.get().get(identifier.name());
        } else if (expression instanceof Ast.FunctionCall functionCall) {
            var definition = functionEnvironment.get(functionCall.name());
            if(definition == null) {
                throw new LanguageException("Function " + functionCall.name() + " is not found");
            }

            var actualParams = functionCall.args();
            var formalParams= definition.args();
            var body = definition.body();
            var values = actualParams.stream().map(this::interpret).collect(Collectors.toList());
            var backup = variableEnvironment;
            variableEnvironment = newEnvironment(Optional.of(variableEnvironment));
            int i = 0;
            for(var formalParamName : formalParams) {
                variableEnvironment.bindings().put(formalParamName, values.get(i));
                i++;
            }
            var result = interpret(body);
            variableEnvironment = backup;
            return result;
        }else if(expression instanceof Ast.Assignment assignment) {
            var bindingsOpt= variableEnvironment.findBinding(assignment.name());
            Value value = interpret(assignment.expression());
            if(bindingsOpt.isPresent()) {
                bindingsOpt.get().put(assignment.name(), value);
            } else {
                variableEnvironment.bindings().put(assignment.name(), value);
            }
            return value;
        } else if (expression instanceof Ast.BlockExpression block) {
            Value value = null;
            for(var e : block.elements()) {
                value = interpret(e);
            }
            return value;
        } else if(expression instanceof Ast.Println println) {
            return interpret(println.arg());
        } else if(expression instanceof Ast.IfExpression ifExpression) {
            boolean satisfied= interpret(ifExpression.condition()).asBool().value();
            if(satisfied) {
                return interpret(ifExpression.thenClause());
            } else {
                var elseClauseOpt = ifExpression.elseClause();
                return elseClauseOpt.map(this::interpret).orElse(null);
            }
        } else if(expression instanceof Ast.WhileExpression whileExpression) {
            while (true) {
                boolean satisfied = interpret(whileExpression.condition()).asBool().value();
                if (satisfied) {
                    interpret(whileExpression.body());
                } else {
                    break;
                }
            }
            return wrap(true);
        } else if (expression instanceof Ast.LabelledCall labelledCall) {
            var definition = functionEnvironment.get(labelledCall.name());
            if (definition == null) {
                throw new RuntimeException("Function " + labelledCall.name() + " is not found");
            }
            var labels = labelledCall.args();
            var mapping = new HashMap<String, Ast.Expression>();
            for(var label:labels) {
                mapping.put(label.name(), label.parameter());
            }
            var formalParams = definition.args();
            var actualParams = new ArrayList<Ast.Expression>();
            for(var param:formalParams) {
                actualParams.add(mapping.get(param));
            }
            var body = definition.body();
            var values = actualParams.stream().map(this::interpret).collect(Collectors.toList());
            var backup = variableEnvironment;
            variableEnvironment = newEnvironment(Optional.of(variableEnvironment));
            int i = 0;
            for (var formalParamName : formalParams) {
                variableEnvironment.bindings().put(formalParamName, values.get(i));
                i++;
            }
            var result = interpret(body);
            variableEnvironment = backup;
            return result;
        } else if(expression instanceof Ast.ArrayLiteral arrayLiteral) {
            var items = arrayLiteral.items().stream().map(this::interpret).collect((Collectors.toList()));
            return wrap(items);
        } else if(expression instanceof Ast.BoolLiteral boolLiteral) {
            return wrap(boolLiteral.value());
        } else {
            throw new RuntimeException("must not reach here");
        }
    }

    public Value callMain(Ast.Program program) {
        var topLevels = program.definitions();
        for(var topLevel : topLevels) {
            if(topLevel instanceof Ast.GlobalVariableDefinition globalVariableDefinition) {
                variableEnvironment.bindings().put(
                        globalVariableDefinition.name(),
                        interpret(globalVariableDefinition.expression())
                );
            } else if(topLevel instanceof Ast.FunctionDefinition functionDefinition) {
                functionEnvironment.put(functionDefinition.name(), functionDefinition);
            }
        }
        var mainFunction = functionEnvironment.get("main");
        if(mainFunction != null) {
            return interpret(mainFunction.body());
        } else {
            throw new LanguageException("This program doesn't have main() function");
        }
    }
}
