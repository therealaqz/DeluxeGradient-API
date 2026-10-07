// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import org.jetbrains.annotations.NotNull;

/**
 * A node in a parsed format tree.
 *
 * <p>The tree is the single intermediate representation between every input
 * syntax and every output target. Parsers produce it, renderers consume it, and
 * neither knows about the other. Adding {@code &amp;#RRGGBB} support touches
 * only the lexer; adding a new output format touches only a renderer.
 *
 * <p>Implementations are immutable. A parsed tree is built once at config load
 * and shared across threads without synchronisation.
 */
public interface Node {

    /**
     * Double dispatch into a renderer or analyser.
     *
     * <p>Java 8 has neither sealed types nor pattern matching, so a visitor is
     * how output targets dispatch by node type. Interaction scopes have transparent
     * defaults so existing text-only visitors remain compatible.
     */
    <R> R accept(@NotNull NodeVisitor<R> visitor);
}
