package ru.strakhov.devs.generator;

import ru.strakhov.devs.analyzer.LexicalAnalyzer;
import ru.strakhov.devs.analyzer.SemanticAnalyzer;
import ru.strakhov.devs.lexical_object.entity.LexcialObject;
import ru.strakhov.devs.lexical_object.entity.VariableType;
import ru.strakhov.devs.lexical_object.type.IdentifierType;
import ru.strakhov.devs.lexical_object.type.NumberType;
import ru.strakhov.devs.manager.IOFileManager;
import ru.strakhov.devs.parser.syntax.SyntaxParser;
import ru.strakhov.devs.parser.syntax.SyntaxTreeNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostfixCodeGenerator {
    private final LexicalAnalyzer lexicalAnalyzer;
    private final List<String> postfixTokens = new ArrayList<>();
    private final Map<String, Integer> symbolTable = new LinkedHashMap<>();
    private final Map<String, VariableType> variableTypes = new LinkedHashMap<>();

    public PostfixCodeGenerator(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        // Копируем таблицу символов из лексического анализатора
        Map<String, Integer> originalSymbolTable = lexicalAnalyzer.getSymbolTable();
        Map<String, VariableType> originalVariableTypes = lexicalAnalyzer.getVariableTypes();
        
        symbolTable.putAll(originalSymbolTable);
        variableTypes.putAll(originalVariableTypes);
    }

    /**
     * Генерирует постфиксную нотацию из синтаксического дерева
     */
    public void generate(SyntaxTreeNode root) {
        if (root == null) return;
        
        // Сначала выполняем семантический анализ для получения дерева с Int2Float
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(lexicalAnalyzer);
        SyntaxTreeNode modifiedTree = semanticAnalyzer.analyze(root);
        
        // Генерируем постфиксную нотацию из модифицированного дерева
        generatePostfix(modifiedTree);
    }

    /**
     * Рекурсивно генерирует постфиксную нотацию из узла дерева
     */
    private void generatePostfix(SyntaxTreeNode node) {
        if (node == null) return;

        String value = node.getValue();
        
        // Если это листовой узел (операнд)
        if (node.getChildren().isEmpty()) {
            LexcialObject token = node.getNode();
            if (token == null) return;

            // Если это идентификатор
            if (token.getType() instanceof IdentifierType) {
                postfixTokens.add(formatIdentifier(token.getIdentifierId()));
                return;
            }
            
            // Если это число
            if (token.getType() instanceof NumberType) {
                postfixTokens.add(String.format("<%s>", token.getValue()));
                return;
            }
            
            return;
        }

        // Если это узел Int2Float
        if ("Int2Float".equals(value)) {
            // Сначала обрабатываем операнд
            generatePostfix(node.getChildren().get(0));
            // Затем добавляем операцию конвертации
            postfixTokens.add("<i2f>");
            return;
        }

        // Если это бинарная операция
        if (isBinaryOperation(value)) {
            // Сначала обрабатываем левый операнд
            generatePostfix(node.getChildren().get(0));
            // Затем правый операнд
            generatePostfix(node.getChildren().get(1));
            // Затем добавляем операцию
            postfixTokens.add(String.format("<%s>", value));
            return;
        }

        // Если это унарный минус
        if ("-".equals(value) && node.getChildren().size() == 1) {
            // Обрабатываем операнд
            generatePostfix(node.getChildren().get(0));
            // Добавляем операцию унарного минуса
            postfixTokens.add("<-u>");
            return;
        }
    }

    /**
     * Форматирует идентификатор как <id,N>
     */
    private String formatIdentifier(int id) {
        return String.format("<id,%d>", id);
    }

    /**
     * Проверяет, является ли значение бинарной операцией
     */
    private boolean isBinaryOperation(String value) {
        return "+".equals(value) || "-".equals(value) || 
               "*".equals(value) || "/".equals(value);
    }

    /**
     * Сохраняет постфиксную нотацию в файл
     */
    public void saveToFile(String filename) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < postfixTokens.size(); i++) {
            builder.append(postfixTokens.get(i));
            if (i < postfixTokens.size() - 1) {
                builder.append(" ");
            }
        }
        builder.append("\n");
        IOFileManager.createFile(filename, builder.toString());
    }

    /**
     * Сохраняет таблицу символов в файл
     */
    public void saveSymbolsToFile(String filename) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            String varName = entry.getKey();
            Integer id = entry.getValue();
            VariableType varType = variableTypes.get(varName);
            
            String typeName = varType == VariableType.FLOAT ? "float" : "integer";
            builder.append(String.format("<id,%d> - %s, %s\n", id, varName, typeName));
        }
        IOFileManager.createFile(filename, builder.toString());
    }
}
