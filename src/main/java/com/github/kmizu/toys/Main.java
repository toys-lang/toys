package com.github.kmizu.toys;

import org.javafp.parsecj.input.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.out.println("""
                    Usage: java -jar toys.jar <fileName>.toys
                    """);
        }
        var fileName = args[0];
        var content = Files.readString(Paths.get(fileName));
        var program = Parsers.program().parse(Input.of(content)).getResult();
        var interpreter = new Interpreter();
        System.out.println(interpreter.callMain(program));
    }
}
