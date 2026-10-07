// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import org.jetbrains.annotations.NotNull;

/**
 * Visitor over every node type in a format tree.
 *
 * @param <R> the result type. Renderers that accumulate into a builder use
 *            {@link Void}; analysers such as the character counter return a
 *            value.
 */
public interface NodeVisitor<R> {

    /** Old renderers and third-party visitors retain visible children without events. */
    default R visitClick(final @NotNull ClickNode node) {
        return visitGroup(Nodes.group(node.children()));
    }

    default R visitHover(final @NotNull HoverNode node) {
        return visitGroup(Nodes.group(node.children()));
    }

    R visitText(@NotNull TextNode node);

    R visitGroup(@NotNull GroupNode node);

    R visitColor(@NotNull ColorNode node);

    R visitGradient(@NotNull GradientNode node);

    R visitDecoration(@NotNull DecorationNode node);

    R visitReset(@NotNull ResetNode node);

    R visitPlaceholder(@NotNull PlaceholderNode node);
}
