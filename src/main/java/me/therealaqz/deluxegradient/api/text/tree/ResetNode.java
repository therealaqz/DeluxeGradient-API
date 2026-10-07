// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import org.jetbrains.annotations.NotNull;

/**
 * Discards all inherited colour and decoration from this point on.
 *
 * <p>Produced by {@code &r} and {@code <reset>}. It is a leaf, not a scope: it
 * marks a boundary in the sibling sequence rather than wrapping anything.
 */
public final class ResetNode implements Node {

    static final ResetNode INSTANCE = new ResetNode();

    private ResetNode() {
    }

    @Override
    public <R> R accept(final @NotNull NodeVisitor<R> visitor) {
        return visitor.visitReset(this);
    }

    @Override
    public String toString() {
        return "Reset";
    }
}
