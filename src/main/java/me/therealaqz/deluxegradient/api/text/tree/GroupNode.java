// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A sequence of children that applies no styling of its own.
 *
 * <p>Used as the tree root and wherever the parser needs a container without a
 * meaning attached.
 */
public final class GroupNode implements Node {

    private final List<Node> children;

    GroupNode(final @NotNull List<Node> children) {
        this.children = Nodes.copyOf(children);
    }

    public @NotNull List<Node> children() {
        return this.children;
    }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) {
        return visitor.visitGroup(this);
    }

    @Override
    public String toString() {
        return "Group" + this.children;
    }
}
