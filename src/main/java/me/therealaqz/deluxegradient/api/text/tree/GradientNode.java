// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import me.therealaqz.deluxegradient.api.color.Gradient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Spreads a colour ramp across the visible characters of its subtree.
 *
 * <p>Unlike every other node, this one cannot be rendered by looking at itself.
 * The colour of a character depends on how many characters the whole subtree
 * contains, so a renderer has to measure the subtree first and then walk it.
 * That two pass requirement is the reason renderers hold state rather than
 * being pure functions over nodes.
 *
 * <p>Nested gradients are permitted and the inner one wins for its own range,
 * which is what lets an admin highlight one word inside an otherwise gradient
 * message.
 */
public final class GradientNode implements Node {

    private final Gradient gradient;
    private final List<Node> children;

    GradientNode(final @NotNull Gradient gradient, final @NotNull List<Node> children) {
        this.gradient = gradient;
        this.children = Nodes.copyOf(children);
    }

    public @NotNull Gradient gradient() {
        return this.gradient;
    }

    public @NotNull List<Node> children() {
        return this.children;
    }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) {
        return visitor.visitGradient(this);
    }

    @Override
    public String toString() {
        return "Gradient(" + this.gradient + ')' + this.children;
    }
}
