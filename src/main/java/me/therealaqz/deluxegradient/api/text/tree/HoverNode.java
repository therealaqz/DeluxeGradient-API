// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/** A parsed show_text tooltip. The tooltip is not part of the visible gradient length. */
public final class HoverNode implements Node {
    private final Node text;
    private final List<Node> children;

    HoverNode(final Node text, final List<Node> children) {
        this.text = text;
        this.children = Nodes.copyOf(children);
    }

    public Node text() { return this.text; }
    public List<Node> children() { return this.children; }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) { return visitor.visitHover(this); }
}
