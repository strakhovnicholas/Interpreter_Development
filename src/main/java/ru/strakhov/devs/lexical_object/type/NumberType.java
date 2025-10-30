package ru.strakhov.devs.lexical_object.type;

import ru.strakhov.devs.lexical_object.abstractions.LexicalObjectType;

public class NumberType extends LexicalObjectType {
    public NumberType() {
        super("^[+-]?(?:0|[1-9]\\d*)(?:\\.\\d+)?$");
    }
}
