package ru.strakhov.devs.analyzer;

import ru.strakhov.devs.parser.syntax.SyntaxTreeNode;
import ru.strakhov.devs.lexical_object.type.IdentifierType;
import ru.strakhov.devs.parser.syntax.SyntaxParser;
import ru.strakhov.devs.validator.TokenValidator;
import ru.strakhov.devs.lexical_object.entity.LexcialObject;
import ru.strakhov.devs.lexical_object.entity.VariableType;
import ru.strakhov.devs.factory.LexicalObjectsFactory;
import ru.strakhov.devs.manager.IOFileManager;
import ru.strakhov.devs.visualizer.TreeVisualizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LexicalAnalyzer {
    private static final Set<Character> OPERATORS = Set.of('+', '-', '*', '/', '(', ')');
    private final Map<String, Integer> symbolTable = new LinkedHashMap<>(); // Таблица символов: имя -> ID
    private final Map<String, VariableType> variableTypes = new LinkedHashMap<>(); // Типы переменных: имя -> тип
    private int nextIdentifierId = 1;

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
        // Проверяем, является ли токен идентификатором с типом в квадратных скобках
        if (extractedString.matches("[A-Za-z_][A-Za-z0-9_]*\\[[fFiI]\\]")) {
            // Извлекаем имя переменной и тип
            int bracketIndex = extractedString.indexOf('[');
            String varName = extractedString.substring(0, bracketIndex);
            String typeChar = extractedString.substring(bracketIndex + 1, bracketIndex + 2).toLowerCase();
            
            VariableType varType = typeChar.equals("f") ? VariableType.FLOAT : VariableType.INTEGER;
            
            // Добавляем в таблицу символов, если еще нет
            if (!symbolTable.containsKey(varName)) {
                symbolTable.put(varName, nextIdentifierId++);
                variableTypes.put(varName, varType);
            } else {
                // Обновляем тип, если переменная уже есть
                variableTypes.put(varName, varType);
            }
            
            // Создаем лексический объект с именем переменной (без типа в скобках)
            LexcialObject lexcialObject = LexicalObjectsFactory
                    .createLexicalObject(varName, index);
            
            // Устанавливаем тип переменной и ID
            lexcialObject.setVariableType(varType);
            lexcialObject.setIdentifierId(symbolTable.get(varName));
            
            return lexcialObject;
        }
        
        // Обычный токен
        LexcialObject lexcialObject = LexicalObjectsFactory
                .createLexicalObject(extractedString, index);
        
        // Если это идентификатор без типа, добавляем в таблицу символов с типом INTEGER по умолчанию
        if (lexcialObject.getType() instanceof IdentifierType) {
            String varName = extractedString;
            if (!symbolTable.containsKey(varName)) {
                symbolTable.put(varName, nextIdentifierId++);
                variableTypes.put(varName, VariableType.INTEGER); // По умолчанию целый тип
            }
            // Используем тип из таблицы символов (может быть обновлен, если переменная встречалась с аннотацией типа)
            lexcialObject.setVariableType(variableTypes.get(varName));
            lexcialObject.setIdentifierId(symbolTable.get(varName));
        }
        
        return lexcialObject;
    }

    private void validateExtractedString(String extractedString, int inlinePosition) {
        TokenValidator.validateString(extractedString, inlinePosition);
    }


    public void createSymbolsFile(String symbolsFile, List<LexcialObject> tokens) {
        StringBuilder builder = new StringBuilder();
        // Используем таблицу символов для вывода в правильном порядке
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            String varName = entry.getKey();
            Integer id = entry.getValue();
            VariableType varType = variableTypes.get(varName);
            builder.append(String.format("%d – %s [%s]\n", id, varName, varType.getRussianName()));
        }
        IOFileManager.createFile(symbolsFile, builder.toString());
    }

    public void createTokensFile(String tokensFile, List<LexcialObject> tokens) {
        StringBuilder builder = new StringBuilder();
        tokens.forEach(token -> {
            if (token.getType() instanceof IdentifierType) {
                // Формат: <id,1> - идентификатор с именем var1 вещественного типа
                String typeInfo = token.getVariableType() == VariableType.FLOAT ? "вещественного типа" : "целого типа";
                builder.append(String.format("<id,%d>\t- идентификатор с именем %s %s\n", 
                    token.getIdentifierId(), token.getValue(), typeInfo));
            } else {
                // Для остальных токенов используем стандартный формат
                builder.append(String.format("<%s>\n", token.getValue()));
            }
        });
        IOFileManager.createFile(tokensFile, builder.toString());
    }
    
    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }
    
    public Map<String, VariableType> getVariableTypes() {
        return variableTypes;
    }

}
