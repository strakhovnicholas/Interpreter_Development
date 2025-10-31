package ru.strakhov.devs;

import ru.strakhov.devs.analyzer.LexicalAnalyzer;
import ru.strakhov.devs.starter.ExpressionProcessor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java Main <mode> <input_file>");
            System.exit(1);
        }

        String mode = args[0].toLowerCase();
        String inputFile = args[1];
        String tokensFile = args[2];
        String symbolsFile = args[3];

        ExpressionProcessor expressionProcessor = new ExpressionProcessor();
        if (mode.equals("lex")) {
            expressionProcessor.setCurrentMode("lex");
            expressionProcessor.start(inputFile, tokensFile, symbolsFile);
        } else if (mode.equals("syn")){
            expressionProcessor.setCurrentMode("syn");
            expressionProcessor.start(inputFile, tokensFile, symbolsFile);
        } else {
            System.err.println("Invalid mode. Use LEX or SYN.");
            System.exit(1);
        }
    }
}