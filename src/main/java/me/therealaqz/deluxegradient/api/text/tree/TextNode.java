// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import org.jetbrains.annotations.NotNull;

/**
 * A run of literal characters. The only node that contributes to the visible
 * length of a message, which is what gradients divide themselves across.
 */
public final class TextNode implements Node {

    private final String content;
    private final boolean atomic;

    TextNode(final @NotNull String content, final boolean atomic) {
        this.content = content;
        this.atomic = atomic;
    }

    public @NotNull String content() {
        return this.content;
    }

    public int length() {
        return this.content.length();
    }

    /**
     * True when no colour code may be emitted <em>inside</em> this run.
     *
     * <p>This exists for tokens that a later plugin in the chain still has to
     * recognise. ChatControl's {@code [item]}, for instance, is substituted
     * after our output has been handed on, and it is matched literally. A
     * gradient that inserts a fourteen character colour code between the
     * {@code [} and the {@code i} destroys the token, and the player sees
     * {@code [item]} in chat instead of their held item.
     *
     * <p>An atomic run still occupies its full length in the gradient, so the
     * characters after it are coloured exactly as if it had been ordinary text.
     * Counting it as one character instead would trade a broken token for
     * colours that visibly jump.
     *
     * <p>
     */
    public boolean isAtomic() {
        return this.atomic;
    }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) {
        return visitor.visitText(this);
    }

    @Override
    public String toString() {
        return (this.atomic ? "Atomic(" : "Text(") + this.content + ')';
    }
}
