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
                ALLOWED_SYMBOL_PATTERN.matcher(String.valueOf(c)).matches();
    }

}
