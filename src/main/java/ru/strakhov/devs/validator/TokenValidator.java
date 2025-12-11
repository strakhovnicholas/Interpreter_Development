package ru.strakhov.devs.validator;

import ru.strakhov.devs.exception.IdentifierException;
import ru.strakhov.devs.exception.IllegalConstantTypeException;
import ru.strakhov.devs.exception.IllegalSymbolException;
import ru.strakhov.devs.lexical_object.type.IdentifierType;
import ru.strakhov.devs.lexical_object.type.NumberType;

import java.util.Set;
import java.util.regex.Pattern;

public class TokenValidator {
    private static final Pattern ALLOWED_SYMBOL_PATTERN = Pattern.compile("[a-zA-Z0-9_.]");
    private static final Set<Character> OPERATORS = Set.of('+', '-', '*', '/', '(', ')');

    public static void validateString(String input, int inLinePosition) {
        // Проверяем, является ли это идентификатором с типом в квадратных скобках
        if (input.matches("[A-Za-z_][A-Za-z0-9_]*\\[[fFiI]\\]")) {
            // Валидация для идентификатора с типом
            int bracketIndex = input.indexOf('[');
            String varName = input.substring(0, bracketIndex);
            String typePart = input.substring(bracketIndex);
            
            // Проверяем имя переменной
            for (int i = 0; i < varName.length(); i++) {
                char c = varName.charAt(i);
                if (!isValidSymbol(c)) {
                    throw new IllegalSymbolException(
                            String.format("Недопустимый символ «%s» на позиции %d", c, inLinePosition + i)
                    );
                }
            }
            
            // Проверяем часть с типом [f], [F], [i], или [I]
            if (!typePart.matches("\\[[fFiI]\\]")) {
                throw new IllegalSymbolException(
                        String.format("Неправильный формат типа переменной «%s» на позиции %d. Используйте [f], [F], [i] или [I]", 
                                typePart, inLinePosition + bracketIndex)
                );
            }
            
            TokenValidator.validateFullString(varName, inLinePosition);
            return;
        }
        
        // Обычная валидация
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (!isValidSymbol(c)) {
                throw new IllegalSymbolException(
                        String.format("Недопустимый символ «%s» на позиции %d", c, inLinePosition + i)
                );
            }
        }
        TokenValidator.validateFullString(input, inLinePosition);
    }

    private static void validateFullString(String input, int inLinePosition) {
        boolean containsDigit = input.chars().anyMatch(Character::isDigit);
        boolean containsLetter = input.chars().anyMatch(Character::isLetter);

        if (containsDigit && !containsLetter) {
            if (!isStringTrueDigit(input)) {
                throw new IllegalConstantTypeException(
                        String.format("Неправильно задана константа «%s» на позиции %d", input, inLinePosition)
                );
            }
        } else if (containsDigit && containsLetter) {
            if (!new IdentifierType().getPattern().matcher(input).matches()) {
                throw new IdentifierException(
                        String.format("Идентификатор «%s» не может начинаться с цифры на позиции: %d", input, inLinePosition)
                );
            }
        }
    }

    private static boolean isStringTrueDigit(String input) {
        return new NumberType().getPattern().matcher(input).matches();
    }

    private static boolean isValidSymbol(char c) {
        return OPERATORS.contains(c) ||
                ALLOWED_SYMBOL_PATTERN.matcher(String.valueOf(c)).matches() ||
                c == '[' || c == ']'; // Разрешаем квадратные скобки для типов переменных
    }

}
