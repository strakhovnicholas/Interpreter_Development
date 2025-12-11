package ru.strakhov.devs;

import ru.strakhov.devs.analyzer.LexicalAnalyzer;
import ru.strakhov.devs.starter.ExpressionProcessor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java Main <mode> <input_file> [tokens_file] [symbols_file]");
            System.exit(1);
        }

        String mode = args[0].toLowerCase();
        String inputFile = args[1];
        String tokensFile = args.length > 2 ? args[2] : null;
        String symbolsFile = args.length > 3 ? args[3] : null;

        ExpressionProcessor expressionProcessor = new ExpressionProcessor();
        if (mode.equals("lex")) {
            if (tokensFile == null || symbolsFile == null) {
                System.err.println("For LEX mode, tokens_file and symbols_file are required");
                System.exit(1);
            }
            expressionProcessor.setCurrentMode("lex");
            expressionProcessor.start(inputFile, tokensFile, symbolsFile);
        } else if (mode.equals("syn")){
            expressionProcessor.setCurrentMode("syn");
            expressionProcessor.start(inputFile, tokensFile, symbolsFile);
        } else if (mode.equals("sem")) {
            expressionProcessor.setCurrentMode("sem");
            expressionProcessor.start(inputFile, tokensFile, symbolsFile);
        } else if (mode.equals("gen1")) {
            expressionProcessor.setCurrentMode("gen1");
            expressionProcessor.start(inputFile, tokensFile, symbolsFile);
        } else if (mode.equals("gen2")) {
            expressionProcessor.setCurrentMode("gen2");
            expressionProcessor.start(inputFile, tokensFile, symbolsFile);
        } else {
            System.err.println("Invalid mode. Use LEX, SYN, SEM, GEN1, or GEN2:)");
            System.exit(1);
        }
    }
}