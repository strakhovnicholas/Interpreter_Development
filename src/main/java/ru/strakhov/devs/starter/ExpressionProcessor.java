package ru.strakhov.devs.starter;

import ru.strakhov.devs.analyzer.LexicalAnalyzer;
import ru.strakhov.devs.analyzer.SemanticAnalyzer;
import ru.strakhov.devs.generator.PostfixCodeGenerator;
import ru.strakhov.devs.generator.ThreeAddressCodeGenerator;
import ru.strakhov.devs.lexical_object.entity.LexcialObject;
import ru.strakhov.devs.manager.IOFileManager;
import ru.strakhov.devs.parser.syntax.SyntaxParser;
import ru.strakhov.devs.parser.syntax.SyntaxTreeNode;
import ru.strakhov.devs.visualizer.TreeVisualizer;

import java.io.IOException;
import java.util.List;

public class ExpressionProcessor {
    private String currentMode;

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }

    public void start(String inputFileName, String tokensFile, String symbolsFile) throws IOException {
        String fileExpression = IOFileManager.readFile(inputFileName);
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        List<LexcialObject> tokens = lexicalAnalyzer.getTokens(fileExpression);

        if (currentMode.equals("lex")) {
            this.createLexicalFiles(lexicalAnalyzer, tokens, tokensFile, symbolsFile);
        } else if (currentMode.equals("syn")) {
            this.createSyntaxTreeFile(tokens);
        } else if (currentMode.equals("sem")) {
            this.createSemanticTreeFile(lexicalAnalyzer, tokens);
        } else if (currentMode.equals("gen1")) {
            this.createThreeAddressCode(lexicalAnalyzer, tokens);
        } else if (currentMode.equals("gen2")) {
            this.createPostfixCode(lexicalAnalyzer, tokens);
        }
    }

    private void createSyntaxTreeFile(List<LexcialObject> tokens) throws IOException {
        SyntaxParser parser = new SyntaxParser(tokens);
        SyntaxTreeNode tree = parser.parseExpression();
        parser.checkEnd();

        TreeVisualizer.printTreeToFile(tree, "syntax_tree.txt");
    }

    private void createSemanticTreeFile(LexicalAnalyzer lexicalAnalyzer, List<LexcialObject> tokens) throws IOException {
        SyntaxParser parser = new SyntaxParser(tokens);
        SyntaxTreeNode tree = parser.parseExpression();
        parser.checkEnd();

        // Выполняем семантический анализ
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(lexicalAnalyzer);
        SyntaxTreeNode modifiedTree = semanticAnalyzer.analyze(tree);

        // Сохраняем модифицированное дерево
        TreeVisualizer.printTreeToFile(modifiedTree, "syntax_tree_mod.txt");
    }

    private void createLexicalFiles(LexicalAnalyzer lexicalAnalyzer, List<LexcialObject> tokens , String tokensFile, String symbolsFile) throws IOException {
        lexicalAnalyzer.createTokensFile(tokensFile, tokens);
        lexicalAnalyzer.createSymbolsFile(symbolsFile, tokens);
    }

    private void createThreeAddressCode(LexicalAnalyzer lexicalAnalyzer, List<LexcialObject> tokens) throws IOException {
        SyntaxParser parser = new SyntaxParser(tokens);
        SyntaxTreeNode tree = parser.parseExpression();
        parser.checkEnd();

        // Генерируем трехадресный код
        ThreeAddressCodeGenerator codeGenerator = new ThreeAddressCodeGenerator(lexicalAnalyzer);
        codeGenerator.generate(tree);
        codeGenerator.saveToFile("portable_code.txt");
        codeGenerator.saveSymbolsToFile("symbols.txt");
    }

    private void createPostfixCode(LexicalAnalyzer lexicalAnalyzer, List<LexcialObject> tokens) throws IOException {
        SyntaxParser parser = new SyntaxParser(tokens);
        SyntaxTreeNode tree = parser.parseExpression();
        parser.checkEnd();

        // Генерируем постфиксную нотацию
        PostfixCodeGenerator postfixGenerator = new PostfixCodeGenerator(lexicalAnalyzer);
        postfixGenerator.generate(tree);
        postfixGenerator.saveToFile("postfix.txt");
        postfixGenerator.saveSymbolsToFile("symbols.txt");
    }
}
