// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import org.jetbrains.annotations.NotNull;

/**
 * A placeholder whose value is not known at parse time.
 *
 * <p>Chat formats contain things like {@code %player_name%} and
 * {@code {message}}. Those cannot be folded into a {@link TextNode} at config
 * load, because their length changes per message and a gradient spread depends
 * on length.
 *
 * <p>Keeping them as their own node is what lets the format be parsed once and
 * reused: the renderer substitutes the value, measures it, and only then
 * decides the colour of each character. Baking placeholders into text would
 * force a full re-parse per message, which is the single most common
 * performance mistake in this category of plugin.
 */
public final class PlaceholderNode implements Node {

    private final String identifier;

    PlaceholderNode(final @NotNull String identifier) {
        this.identifier = identifier;
    }

    /**
     * The placeholder name without its delimiters, for example
     * {@code player_name} or {@code message}.
     */
    public @NotNull String identifier() {
        return this.identifier;
    }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) {
        return visitor.visitPlaceholder(this);
    }

    @Override
    public String toString() {
        return "Placeholder(" + this.identifier + ')';
    }
}
