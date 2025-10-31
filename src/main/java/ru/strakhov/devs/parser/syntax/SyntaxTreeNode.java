package ru.strakhov.devs.parser.syntax;

import ru.strakhov.devs.lexical_object.entity.LexcialObject;

import java.util.ArrayList;
import java.util.List;

public class SyntaxTreeNode {
    private String value;
    public List<SyntaxTreeNode> children = new ArrayList<>();
    private LexcialObject node;

    public SyntaxTreeNode(String value, LexcialObject node) {
        this.value = value;
        this.node = node;
    }

    public String getValue() {
        return value;
    }

    public List<SyntaxTreeNode> getChildren() {
        return children;
    }

    public LexcialObject getNode() {
        return node;
    }
}
