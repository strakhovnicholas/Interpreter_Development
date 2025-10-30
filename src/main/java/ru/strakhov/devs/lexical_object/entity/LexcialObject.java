package ru.strakhov.devs.lexical_object.entity;

import lombok.Builder;
import lombok.Data;
import ru.strakhov.devs.lexical_object.abstractions.LexicalObjectType;

@Builder
@Data
public class LexcialObject {
    private String value;
    private int inLinePosition;
    private LexicalObjectType type;

    @Override
    public String toString() {
        return String.format("<%s> - %s", this.value, this.type);
    }
}
