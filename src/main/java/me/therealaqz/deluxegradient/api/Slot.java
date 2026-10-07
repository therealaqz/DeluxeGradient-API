// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * The two things a player can colour independently.
 *
 * <p>Kept separate because they are not equally ours to own. A name is a leaf
 * value that gets substituted into someone else's format, so colouring it is
 * always safe. A message body is contested territory whenever another chat
 * plugin is installed.
 *
 * <p>Every slot-taking method on {@link DeluxeGradientApi} needs to name one of
 * these two values. Spelling that as a {@code String} put the burden of getting
 * it right on the caller and turned a typo into {@code null} or an empty set
 * rather than a compile error, which is the wrong trade in an interface that is
 * frozen once it is published.
 */
public enum Slot {

    CHAT("chat"),
    NAME("name");

    private final String id;

    Slot(final String id) {
        this.id = id;
    }

    /**
     * Lower case identifier, used in storage columns and permission nodes.
     *
     * <p>Stable. It is written into player data files and into permission
     * nodes owners have already granted, so it is not free to change.
     */
    public @NotNull String id() {
        return this.id;
    }

    /**
     * Resolves an identifier case insensitively.
     *
     * <p>Kept because slots still arrive as text from commands, config files
     * and placeholders. API callers have the constants and should use them.
     *
     * @return the slot, or {@code null} if the text names neither
     */
    public static @Nullable Slot byId(final @Nullable String id) {
        if (id == null) {
            return null;
        }
        final String lower = id.toLowerCase(Locale.ROOT);
        for (final Slot slot : values()) {
            if (slot.id.equals(lower)) {
                return slot;
            }
        }
        return null;
    }
}
