package ru.strakhov.devs.parser.syntax;

import ru.strakhov.devs.exception.SyntaxException;
import ru.strakhov.devs.lexical_object.entity.LexcialObject;

import java.util.List;
import java.util.Objects;

public class SyntaxParser {
    private final List<LexcialObject> tokens;
    private int index = 0;

    public SyntaxParser(List<LexcialObject> tokens) {
        this.tokens = tokens;
    }

    public SyntaxTreeNode parseExpression() {
        SyntaxTreeNode left = parseTerm();
        while (currentTokenIs("PlusType", "MinusType")) {
            String op = Objects.requireNonNull(currentToken()).getValue();
            advance();
            SyntaxTreeNode right = parseTerm();
            SyntaxTreeNode node = new SyntaxTreeNode(op, currentToken());
            node.children.add(left);
            node.children.add(right);
            left = node;
        }
        return left;
    }

    public SyntaxTreeNode parseTerm() {
        SyntaxTreeNode left = parseFactor();
        while (currentTokenIs("MultiplyType", "DivideType")) {
            String op = Objects.requireNonNull(currentToken()).getValue();
            advance();
            SyntaxTreeNode right = parseFactor();
            SyntaxTreeNode node = new SyntaxTreeNode(op, currentToken());
            node.children.add(left);
            node.children.add(right);
            left = node;
        }
        return left;
    }

    public SyntaxTreeNode parseFactor() {
        LexcialObject current = currentToken();

        if (currentTokenIs("MinusType")) {
            advance();
            SyntaxTreeNode operand = parseFactor();
            SyntaxTreeNode node = new SyntaxTreeNode("-", current);
            node.children.add(operand);
            return node;
        }

        if (currentTokenIs("OpenBracketType")) {
            advance();
            SyntaxTreeNode expr = parseExpression();
            if (!currentTokenIs("CloseBracketType")) {
                throw new SyntaxException("Missing closing parenthesis at index " + index);
            }
            advance();
            return expr;
        }

        if (currentTokenIs("IdentifierType", "NumberType")) {
            advance();
            assert current != null;
            return new SyntaxTreeNode(current.getValue(), current);
        }

        throw new SyntaxException("Expected operand at index " + index);
    }

    private LexcialObject currentToken() {
        if (index >= tokens.size()) return null;
        return tokens.get(index);
    }

    private boolean currentTokenIs(String... typeNames) {
        LexcialObject t = currentToken();
        if (t == null) return false;
        String typeName = t.getType().getClass().getSimpleName();
        for (String name : typeNames) {
            if (typeName.equals(name)) return true;
        }
        return false;
    }

    private void advance() {
        index++;
    }

    public void checkEnd() {
        if (index < tokens.size()) {
            throw new SyntaxException("Unexpected token at index " + index + ": " + currentToken());
        }
    }
}