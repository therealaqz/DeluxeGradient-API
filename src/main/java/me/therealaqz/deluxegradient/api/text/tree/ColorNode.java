// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import me.therealaqz.deluxegradient.api.color.DeluxeColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Applies one solid colour to everything below it.
 *
 * <p>Both {@code <red>text</red>} and the inline {@code &c} produce this node.
 * The difference is only how far the parser lets the scope extend: an explicit
 * tag closes where it says it does, an inline code runs to the end of its
 * enclosing scope.
 */
public final class ColorNode implements Node {

    private final DeluxeColor color;
    private final List<Node> children;

    ColorNode(final @NotNull DeluxeColor color, final @NotNull List<Node> children) {
        this.color = color;
        this.children = Nodes.copyOf(children);
    }

    public @NotNull DeluxeColor color() {
        return this.color;
    }

    public @NotNull List<Node> children() {
        return this.children;
    }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) {
        return visitor.visitColor(this);
    }

    @Override
    public String toString() {
        return "Color(" + this.color + ')' + this.children;
    }
}
