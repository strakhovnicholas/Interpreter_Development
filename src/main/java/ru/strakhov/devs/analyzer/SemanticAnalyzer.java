package ru.strakhov.devs.analyzer;

import ru.strakhov.devs.exception.SyntaxException;
import ru.strakhov.devs.lexical_object.entity.LexcialObject;
import ru.strakhov.devs.lexical_object.entity.VariableType;
import ru.strakhov.devs.lexical_object.type.IdentifierType;
import ru.strakhov.devs.lexical_object.type.NumberType;
import ru.strakhov.devs.parser.syntax.SyntaxTreeNode;

public class SemanticAnalyzer {
    private final LexicalAnalyzer lexicalAnalyzer;

    public SemanticAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    /**
     * Выполняет семантический анализ синтаксического дерева:
     * 1. Добавляет узлы Int2Float для автоматической конвертации типов
     * 2. Проверяет деление на константу 0
     */
    public SyntaxTreeNode analyze(SyntaxTreeNode root) {
        // Проверяем деление на 0
        checkDivisionByZero(root);
        
        // Добавляем конвертацию типов
        return addTypeConversions(root);
    }

    /**
     * Проверяет наличие деления на константу 0 в дереве
     */
    private void checkDivisionByZero(SyntaxTreeNode node) {
        if (node == null) return;

        String value = node.getValue();
        
        // Проверяем, является ли узел операцией деления
        if ("/".equals(value) && node.getChildren().size() == 2) {
            SyntaxTreeNode rightChild = node.getChildren().get(1);
            
            // Проверяем, является ли правый операнд константой 0
            if (rightChild.getNode() != null && 
                rightChild.getNode().getType() instanceof NumberType) {
                try {
                    double numValue = Double.parseDouble(rightChild.getValue());
                    if (numValue == 0.0) {
                        throw new SyntaxException("Ошибка: обнаружено деление на константу 0");
                    }
                } catch (NumberFormatException e) {
                    // Не число, пропускаем
                }
            }
        }

        // Рекурсивно проверяем дочерние узлы
        for (SyntaxTreeNode child : node.getChildren()) {
            checkDivisionByZero(child);
        }
    }

    /**
     * Добавляет узлы Int2Float для автоматической конвертации типов
     * @return Модифицированное дерево
     */
    private SyntaxTreeNode addTypeConversions(SyntaxTreeNode node) {
        if (node == null) return null;

        // Создаем копию узла
        SyntaxTreeNode newNode = new SyntaxTreeNode(node.getValue(), node.getNode());
        
        // Если это листовой узел (операнд), возвращаем копию
        if (node.getChildren().isEmpty()) {
            return newNode;
        }

        // Обрабатываем дочерние узлы рекурсивно
        for (SyntaxTreeNode child : node.getChildren()) {
            newNode.getChildren().add(addTypeConversions(child));
        }

        // Если это бинарная операция, проверяем типы операндов
        if (isBinaryOperation(node.getValue()) && newNode.getChildren().size() == 2) {
            SyntaxTreeNode left = newNode.getChildren().get(0);
            SyntaxTreeNode right = newNode.getChildren().get(1);

            VariableType leftType = getOperandType(left);
            VariableType rightType = getOperandType(right);

            // Если типы разные, добавляем конвертацию
            if (leftType == VariableType.INTEGER && rightType == VariableType.FLOAT) {
                // Левый операнд нужно конвертировать в float
                SyntaxTreeNode int2FloatNode = new SyntaxTreeNode("Int2Float", null);
                int2FloatNode.getChildren().add(left);
                newNode.getChildren().set(0, int2FloatNode);
            } else if (leftType == VariableType.FLOAT && rightType == VariableType.INTEGER) {
                // Правый операнд нужно конвертировать в float
                SyntaxTreeNode int2FloatNode = new SyntaxTreeNode("Int2Float", null);
                int2FloatNode.getChildren().add(right);
                newNode.getChildren().set(1, int2FloatNode);
            }
        }

        return newNode;
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

            // Если это идентификатор, берем тип из таблицы символов
            if (token.getType() instanceof IdentifierType) {
                VariableType varType = token.getVariableType();
                return varType != null ? varType : VariableType.INTEGER;
            }

            // Если это число, определяем тип по значению
            if (token.getType() instanceof NumberType) {
                String value = token.getValue();
                try {
                    double numValue = Double.parseDouble(value);
                    // Если число имеет дробную часть или это вещественное число
                    if (value.contains(".") || value.contains("e") || value.contains("E")) {
                        return VariableType.FLOAT;
                    }
                    // Целые числа по умолчанию
                    return VariableType.INTEGER;
                } catch (NumberFormatException e) {
                    return VariableType.INTEGER;
                }
            }
        }

        // Для операций результат зависит от типов операндов
        // Если есть хотя бы один float операнд, результат float
        if (!node.getChildren().isEmpty()) {
            for (SyntaxTreeNode child : node.getChildren()) {
                VariableType childType = getOperandType(child);
                if (childType == VariableType.FLOAT) {
                    return VariableType.FLOAT;
                }
            }
            // Если все операнды INTEGER, результат INTEGER
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
}
