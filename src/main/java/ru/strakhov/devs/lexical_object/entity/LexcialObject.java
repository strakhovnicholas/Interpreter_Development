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
    private VariableType variableType; // Тип переменной (только для идентификаторов)
    private Integer identifierId; // ID идентификатора в таблице символов

    public String getValue() {
        return value;
    }

    public int getInLinePosition() {
        return inLinePosition;
    }

    public LexicalObjectType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("<%s> - %s", this.value, this.type);
    }
}
