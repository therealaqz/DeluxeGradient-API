// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/** A click action scoped to its visible children. Values may contain placeholders. */
public final class ClickNode implements Node {
    private final ClickAction action;
    private final String value;
    private final List<Node> children;

    ClickNode(final ClickAction action, final String value, final List<Node> children) {
        this.action = action;
        this.value = value;
        this.children = Nodes.copyOf(children);
    }

    public ClickAction action() { return this.action; }
    public String value() { return this.value; }
    public List<Node> children() { return this.children; }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) { return visitor.visitClick(this); }
}
