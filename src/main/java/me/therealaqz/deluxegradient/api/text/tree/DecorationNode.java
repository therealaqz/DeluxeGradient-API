// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import me.therealaqz.deluxegradient.api.text.Decoration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Turns one decoration on or off for its subtree.
 *
 * <p>The {@code enabled} flag exists so {@code <!italic>} can switch italics
 * off inside an italic scope. Minecraft has no "not bold" code, so a renderer
 * targeting legacy strings has to emit a reset and rebuild the surrounding
 * style; the tree records the intent and lets the renderer deal with that.
 */
public final class DecorationNode implements Node {

    private final Decoration decoration;
    private final boolean enabled;
    private final List<Node> children;

    DecorationNode(final @NotNull Decoration decoration, final boolean enabled, final @NotNull List<Node> children) {
        this.decoration = decoration;
        this.enabled = enabled;
        this.children = Nodes.copyOf(children);
    }

    public @NotNull Decoration decoration() {
        return this.decoration;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public @NotNull List<Node> children() {
        return this.children;
    }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) {
        return visitor.visitDecoration(this);
    }

    @Override
    public String toString() {
        return (this.enabled ? "" : "!") + this.decoration.tagName() + this.children;
    }
}
