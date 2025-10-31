package ru.strakhov.devs.analyzer;

import ru.strakhov.devs.parser.syntax.SyntaxTreeNode;
import ru.strakhov.devs.lexical_object.type.IdentifierType;
import ru.strakhov.devs.parser.syntax.SyntaxParser;
import ru.strakhov.devs.validator.TokenValidator;
import ru.strakhov.devs.lexical_object.entity.LexcialObject;
import ru.strakhov.devs.factory.LexicalObjectsFactory;
import ru.strakhov.devs.manager.IOFileManager;
import ru.strakhov.devs.visualizer.TreeVisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LexicalAnalyzer {
    private static final Set<Character> OPERATORS = Set.of('+', '-', '*', '/', '(', ')');

    public List<LexcialObject> getTokens(String line) {
        List<LexcialObject> tokens = new ArrayList<>();
        int index = 0;
        int i = 0;
        line = line.replaceAll("\\s+", "");
        while (i < line.length()) {
            if (OPERATORS.contains(line.charAt(i))) {
                if (index != i) {
                    this.processToken(line, index, i, tokens);
                }
                this.processToken(line, i, i + 1, tokens);
                index = i + 1;
            }
            i++;
        }

        this.processToken(line, index, 0, tokens);


        return tokens;
    }

    private void processToken(String line, int index, int i, List<LexcialObject> tokens) {
        String token = this.extractSubstring(line, index, i);
        if (token.isEmpty()) return;
        this.validateExtractedString(token, index);
        LexcialObject lexcialObject = createLexicalObjectFromExtractedString(token, index);
        tokens.add(lexcialObject);
    }

    private String extractSubstring(String line, int index, int i) {
        String valueBetweenOperators = "";
        if (i != 0) {
            valueBetweenOperators = line.substring(index, i);
        } else {
            valueBetweenOperators = line.substring(index);
        }
        return valueBetweenOperators;
    }

    private LexcialObject createLexicalObjectFromExtractedString(String extractedString, int index) {
        LexcialObject lexcialObject = LexicalObjectsFactory
                .createLexicalObject(extractedString, index);
        return lexcialObject;
    }

    private void validateExtractedString(String extractedString, int inlinePosition) {
        TokenValidator.validateString(extractedString, inlinePosition);
    }


    public void createSymbolsFile(String symbolsFile, List<LexcialObject> tokens) {
        List<LexcialObject> list = tokens
                .stream()
                .filter(token -> token.getType() instanceof IdentifierType)
                .toList();
        StringBuilder builder = getReadyStringBuilder(list);
        IOFileManager.createFile(symbolsFile, builder.toString());
    }

    public void createTokensFile(String tokensFile, List<LexcialObject> tokens) {
        StringBuilder builder = getReadyStringBuilder(tokens);
        IOFileManager.createFile(tokensFile, builder.toString());
    }

    private StringBuilder getReadyStringBuilder(List<LexcialObject> list) {
        StringBuilder builder = new StringBuilder();
        list.forEach(elem -> {
            builder.append(elem);
            builder.append("\n");
        });
        return builder;
    }

}
