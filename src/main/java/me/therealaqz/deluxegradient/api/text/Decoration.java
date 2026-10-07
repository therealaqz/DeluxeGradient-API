// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * The five non-colour text styles.
 *
 * <p>{@code reset} is not one of these. It is not a style that can be on or
 * off, it is an instruction to discard everything, so it is represented as its
 * own node in the tree rather than as a sixth decoration.
 */
public enum Decoration {

    OBFUSCATED('k', "obfuscated", "obf", "magic"),
    BOLD('l', "bold", "b"),
    STRIKETHROUGH('m', "strikethrough", "st", "strike"),
    UNDERLINED('n', "underlined", "underline", "u"),
    ITALIC('o', "italic", "i", "em");

    private static final Decoration[] VALUES = values();

    private final char code;
    private final String[] names;

    Decoration(final char code, final String... names) {
        this.code = code;
        this.names = names;
    }

    public char code() {
        return this.code;
    }

    /**
     * The canonical tag name, which is the first alias.
     */
    public @NotNull String tagName() {
        return this.names[0];
    }

    /**
     * The bit this decoration occupies in a packed style mask.
     */
    public int mask() {
        return 1 << ordinal();
    }

    public static @Nullable Decoration byCode(final char code) {
        final char lower = Character.toLowerCase(code);
        for (final Decoration decoration : VALUES) {
            if (decoration.code == lower) {
                return decoration;
            }
        }
        return null;
    }

    /**
     * Resolves a tag name such as {@code bold}, {@code b} or {@code BOLD}.
     */
    public static @Nullable Decoration byName(final @Nullable String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        final String lower = name.toLowerCase(Locale.ROOT);
        for (final Decoration decoration : VALUES) {
            for (final String alias : decoration.names) {
                if (alias.equals(lower)) {
                    return decoration;
                }
            }
        }
        return null;
    }
}
