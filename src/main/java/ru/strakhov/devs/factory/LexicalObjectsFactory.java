package ru.strakhov.devs.factory;

import ru.strakhov.devs.utils.TypeDetector;
import ru.strakhov.devs.lexical_object.abstractions.LexicalObjectType;
import ru.strakhov.devs.lexical_object.entity.LexcialObject;

public class LexicalObjectsFactory {
    public static LexcialObject createLexicalObject(String value, int position) {
        LexicalObjectType type = TypeDetector.detectType(value);
        return LexcialObject
                .builder()
                .value(value)
                .type(type)
                .inLinePosition(position)
                .build();
    }
}
