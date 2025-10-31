package ru.strakhov.devs.visualizer;

import ru.strakhov.devs.parser.syntax.SyntaxTreeNode;

import java.util.List;

import ru.strakhov.devs.manager.IOFileManager;

public class TreeVisualizer {

    public static void printTreeToFile(SyntaxTreeNode root, String filename) {
        StringBuilder builder = new StringBuilder();
        printTreeRecursive(root, "", true, builder);
        IOFileManager.createFile(filename, builder.toString());
    }

    private static void printTreeRecursive(SyntaxTreeNode node, String prefix, boolean isLast, StringBuilder builder) {
        if (node == null) return;

        builder.append(prefix)
                .append(isLast ? "└── " : "├── ")
                .append("<")
                .append(node.getValue())
                .append(">")
                .append("\n");

        List<SyntaxTreeNode> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }

        for (int i = 0; i < children.size(); i++) {
            boolean isLastChild = (i == children.size() - 1);
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            printTreeRecursive(children.get(i), newPrefix, isLastChild, builder);
        }
    }
}
