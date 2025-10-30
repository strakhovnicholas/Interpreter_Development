package ru.strakhov.devs.lexical_object.abstractions;

import java.util.regex.Pattern;

public abstract class LexicalObjectType {
    protected final Pattern pattern;

    protected LexicalObjectType(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
