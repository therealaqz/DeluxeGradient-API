// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import me.therealaqz.deluxegradient.api.color.DeluxeColor;
import me.therealaqz.deluxegradient.api.color.Gradient;
import me.therealaqz.deluxegradient.api.text.Decoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory methods for the node types.
 *
 * <p>The node classes themselves live in this package as separate files so
 * their javadoc has somewhere to live; this class exists so callers write
 * {@code Nodes.text("hi")} rather than repeating constructor ceremony.
 */
public final class Nodes {

    private Nodes() {
    }

    public static @NotNull TextNode text(final @NotNull String content) {
        return new TextNode(content, false);
    }

    /**
     * Text that must reach the next plugin in the chain byte for byte.
     *
     * @see TextNode#isAtomic()
     */
    public static @NotNull TextNode atomicText(final @NotNull String content) {
        return new TextNode(content, true);
    }

    public static @NotNull GroupNode group(final @NotNull List<Node> children) {
        return new GroupNode(children);
    }

    public static @NotNull ColorNode color(final @NotNull DeluxeColor color, final @NotNull List<Node> children) {
        return new ColorNode(color, children);
    }

    public static @NotNull GradientNode gradient(final @NotNull Gradient gradient, final @NotNull List<Node> children) {
        return new GradientNode(gradient, children);
    }

    public static @NotNull DecorationNode decoration(
            final @NotNull Decoration decoration,
            final boolean enabled,
            final @NotNull List<Node> children
    ) {
        return new DecorationNode(decoration, enabled, children);
    }

    public static @NotNull ClickNode click(final ClickAction action, final String value, final List<Node> children) {
        return new ClickNode(action, value, children);
    }

    public static @NotNull HoverNode hover(final Node text, final List<Node> children) {
        return new HoverNode(text, children);
    }

    public static @NotNull ResetNode reset() {
        return ResetNode.INSTANCE;
    }

    public static @NotNull PlaceholderNode placeholder(final @NotNull String identifier) {
        return new PlaceholderNode(identifier);
    }

    static @NotNull List<Node> copyOf(final @NotNull List<Node> children) {
        return children.isEmpty()
                ? Collections.<Node>emptyList()
                : Collections.unmodifiableList(new ArrayList<Node>(children));
    }
}
