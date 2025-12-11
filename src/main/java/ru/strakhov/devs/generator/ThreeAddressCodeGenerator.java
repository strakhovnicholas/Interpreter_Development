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

public class ThreeAddressCodeGenerator {
    private final LexicalAnalyzer lexicalAnalyzer;
    private final List<String> codeLines = new ArrayList<>();
    private final Map<String, Integer> extendedSymbolTable = new LinkedHashMap<>(); // Расширенная таблица символов
    private final Map<String, VariableType> extendedVariableTypes = new LinkedHashMap<>();
    private int tempVarCounter = 1;
    private int nextSymbolId;

    public ThreeAddressCodeGenerator(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        // Инициализируем расширенную таблицу символов из лексического анализатора
        Map<String, Integer> originalSymbolTable = lexicalAnalyzer.getSymbolTable();
        Map<String, VariableType> originalVariableTypes = lexicalAnalyzer.getVariableTypes();
        
        for (Map.Entry<String, Integer> entry : originalSymbolTable.entrySet()) {
            extendedSymbolTable.put(entry.getKey(), entry.getValue());
            extendedVariableTypes.put(entry.getKey(), originalVariableTypes.get(entry.getKey()));
        }
        
        // Находим максимальный ID для продолжения нумерации
        nextSymbolId = originalSymbolTable.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Генерирует трехадресный код из синтаксического дерева
     */
    public void generate(SyntaxTreeNode root) {
        if (root == null) return;
        
        // Сначала выполняем семантический анализ для получения дерева с Int2Float
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(lexicalAnalyzer);
        SyntaxTreeNode modifiedTree = semanticAnalyzer.analyze(root);
        
        // Генерируем код из модифицированного дерева
        generateCode(modifiedTree);
    }

    /**
     * Рекурсивно генерирует трехадресный код из узла дерева
     * @return имя переменной или константы, содержащей результат
     */
    private String generateCode(SyntaxTreeNode node) {
        if (node == null) return null;

        String value = node.getValue();
        
        // Если это листовой узел (операнд)
        if (node.getChildren().isEmpty()) {
            LexcialObject token = node.getNode();
            if (token == null) return null;

            // Если это идентификатор
            if (token.getType() instanceof IdentifierType) {
                return formatIdentifier(token.getIdentifierId());
            }
            
            // Если это число
            if (token.getType() instanceof NumberType) {
                return token.getValue();
            }
            
            return null;
        }

        // Если это узел Int2Float
        if ("Int2Float".equals(value)) {
            SyntaxTreeNode operand = node.getChildren().get(0);
            String operandName = generateCode(operand);
            
            // Создаем временную переменную для результата конвертации
            String tempVar = createTempVariable(VariableType.FLOAT);
            String tempVarId = formatIdentifier(getSymbolId(tempVar));
            
            codeLines.add(String.format("i2f %s %s", tempVarId, formatOperand(operandName)));
            return tempVar;
        }

        // Если это бинарная операция
        if (isBinaryOperation(value)) {
            SyntaxTreeNode left = node.getChildren().get(0);
            SyntaxTreeNode right = node.getChildren().get(1);
            
            String leftOperand = generateCode(left);
            String rightOperand = generateCode(right);
            
            // Определяем тип результата операции
            VariableType resultType = determineResultType(left, right);
            
            // Создаем временную переменную для результата
            String tempVar = createTempVariable(resultType);
            String tempVarId = formatIdentifier(getSymbolId(tempVar));
            
            // Генерируем код операции
            String opCode = getOperationCode(value);
            codeLines.add(String.format("%s %s %s %s", 
                opCode, tempVarId, 
                formatOperand(leftOperand), 
                formatOperand(rightOperand)));
            
            return tempVar;
        }

        // Если это унарный минус
        if ("-".equals(value) && node.getChildren().size() == 1) {
            SyntaxTreeNode operand = node.getChildren().get(0);
            String operandName = generateCode(operand);
            
            // Для унарного минуса: sub result 0 operand
            VariableType resultType = getOperandType(operand);
            String tempVar = createTempVariable(resultType);
            String tempVarId = formatIdentifier(getSymbolId(tempVar));
            
            codeLines.add(String.format("sub %s 0 %s", tempVarId, formatOperand(operandName)));
            return tempVar;
        }

        return null;
    }

    /**
     * Создает временную переменную с указанным типом
     */
    private String createTempVariable(VariableType type) {
        String tempVarName = "#T" + tempVarCounter++;
        extendedSymbolTable.put(tempVarName, nextSymbolId++);
        extendedVariableTypes.put(tempVarName, type);
        return tempVarName;
    }

    /**
     * Получает ID символа из таблицы
     */
    private int getSymbolId(String varName) {
        return extendedSymbolTable.get(varName);
    }

    /**
     * Форматирует операнд (константа или <id,N>)
     */
    private String formatOperand(String operand) {
        if (operand == null) return null;
        
        // Если это число (константа)
        if (operand.matches("-?\\d+(\\.\\d+)?")) {
            return operand;
        }
        
        // Если это переменная (включая временные)
        if (extendedSymbolTable.containsKey(operand)) {
            return formatIdentifier(extendedSymbolTable.get(operand));
        }
        
        return operand;
    }

    /**
     * Форматирует идентификатор как <id,N>
     */
    private String formatIdentifier(int id) {
        return String.format("<id,%d>", id);
    }

    /**
     * Форматирует идентификатор как <id,N>
     */
    private String formatIdentifier(Integer id) {
        if (id == null) return null;
        return formatIdentifier(id.intValue());
    }

    /**
     * Определяет код операции
     */
    private String getOperationCode(String operation) {
        switch (operation) {
            case "+": return "add";
            case "-": return "sub";
            case "*": return "mul";
            case "/": return "div";
            default: return operation;
        }
    }

    /**
     * Определяет тип результата операции
     */
    private VariableType determineResultType(SyntaxTreeNode left, SyntaxTreeNode right) {
        VariableType leftType = getOperandType(left);
        VariableType rightType = getOperandType(right);
        
        // Если хотя бы один операнд FLOAT, результат FLOAT
        if (leftType == VariableType.FLOAT || rightType == VariableType.FLOAT) {
            return VariableType.FLOAT;
        }
        
        return VariableType.INTEGER;
    }

    /**
     * Определяет тип операнда
     */
    private VariableType getOperandType(SyntaxTreeNode node) {
        if (node == null) return VariableType.INTEGER;

        // Если это узел Int2Float, результат всегда float
        if ("Int2Float".equals(node.getValue())) {
            return VariableType.FLOAT;
        }

        // Если это листовой узел
        if (node.getChildren().isEmpty()) {
            LexcialObject token = node.getNode();
            if (token == null) return VariableType.INTEGER;

            // Если это идентификатор
            if (token.getType() instanceof IdentifierType) {
                VariableType varType = token.getVariableType();
                return varType != null ? varType : VariableType.INTEGER;
            }

            // Если это число
            if (token.getType() instanceof NumberType) {
                String value = token.getValue();
                if (value.contains(".") || value.contains("e") || value.contains("E")) {
                    return VariableType.FLOAT;
                }
                return VariableType.INTEGER;
            }
        }

        // Для операций результат зависит от типов операндов
        if (!node.getChildren().isEmpty()) {
            for (SyntaxTreeNode child : node.getChildren()) {
                VariableType childType = getOperandType(child);
                if (childType == VariableType.FLOAT) {
                    return VariableType.FLOAT;
                }
            }
            return VariableType.INTEGER;
        }

        return VariableType.INTEGER;
    }

    /**
     * Проверяет, является ли значение бинарной операцией
     */
    private boolean isBinaryOperation(String value) {
        return "+".equals(value) || "-".equals(value) || 
               "*".equals(value) || "/".equals(value);
    }

    /**
     * Сохраняет трехадресный код в файл
     */
    public void saveToFile(String filename) {
        StringBuilder builder = new StringBuilder();
        for (String line : codeLines) {
            builder.append(line).append("\n");
        }
        IOFileManager.createFile(filename, builder.toString());
    }

    /**
     * Сохраняет расширенную таблицу символов в файл
     */
    public void saveSymbolsToFile(String filename) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : extendedSymbolTable.entrySet()) {
            String varName = entry.getKey();
            Integer id = entry.getValue();
            VariableType varType = extendedVariableTypes.get(varName);
            
            String typeName = varType == VariableType.FLOAT ? "float" : "integer";
            builder.append(String.format("<id,%d> - %s, %s\n", id, varName, typeName));
        }
        IOFileManager.createFile(filename, builder.toString());
    }
}
