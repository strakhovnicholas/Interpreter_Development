package ru.strakhov.devs;

import ru.strakhov.devs.analyzer.LexicalAnalyzer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        String expressionFile = args[0];
        String tokensFile = args[1];
        String symbolFile = args[2];
        lexicalAnalyzer.start(expressionFile, tokensFile, symbolFile);
    }
}