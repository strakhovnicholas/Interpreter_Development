package ru.strakhov.devs.lexical_object.entity;

public enum VariableType {
    INTEGER("целый"),
    FLOAT("вещественный");

    private final String russianName;

    VariableType(String russianName) {
        this.russianName = russianName;
    }

    public String getRussianName() {
        return russianName;
    }
}
