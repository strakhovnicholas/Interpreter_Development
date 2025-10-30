package ru.strakhov.devs.utils;

import ru.strakhov.devs.lexical_object.abstractions.LexicalObjectType;
import ru.strakhov.devs.lexical_object.type.*;

import java.util.ArrayList;
import java.util.List;

public class TypeDetector {
    private static final List<LexicalObjectType> TYPES = new ArrayList<>();
    static {
        TYPES.add(new OpenBracketType());
        TYPES.add(new CloseBracketType());
        TYPES.add(new PlusType());
        TYPES.add(new MinusType());
        TYPES.add(new MultiplyType());
        TYPES.add(new DivideType());
        TYPES.add(new NumberType());
        TYPES.add(new IdentifierType());
        TYPES.add(new UnknownType());
    }

    public static LexicalObjectType detectType(String value) {
        for (LexicalObjectType type : TYPES) {
            if (type.getPattern().matcher(value).matches()) {
                return type;
            }
        }
        return new UnknownType();
    }
}
