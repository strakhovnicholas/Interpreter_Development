package ru.strakhov.devs.lexical_object.type;

import ru.strakhov.devs.lexical_object.abstractions.LexicalObjectType;

public class IdentifierType extends LexicalObjectType {
    public IdentifierType() {
        super("[A-Za-z_][A-Za-z0-9_]*");
    }
}
