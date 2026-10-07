// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Static access to the API, for contexts where reaching Bukkit's service
 * registry is awkward.
 *
 * <p>Bukkit's {@code ServicesManager} remains the preferred route. This exists
 * because static initialisers, utility classes and command handlers frequently
 * cannot get at a {@code Plugin} instance, and the alternative is every caller
 * writing their own singleton.
 *
 * <p>The instance is set when DeluxeGradient enables and cleared when it disables.
 * A plugin that calls {@link #get()} from its own {@code onEnable} may run
 * first and get nothing, which is why {@link #getOrNull()} exists and why
 * {@code softdepend} on DeluxeGradient is worth adding.
 */
public final class DeluxeGradientProvider {

    private static volatile DeluxeGradientApi instance;

    private DeluxeGradientProvider() {
    }

    /**
     * @throws IllegalStateException if DeluxeGradient is not enabled yet. The
     *         message says what to do about it, because "provider not set" on
     *         its own has sent people looking in the wrong place for years.
     */
    public static @NotNull DeluxeGradientApi get() {
        final DeluxeGradientApi current = instance;
        if (current == null) {
            throw new IllegalStateException(
                    "DeluxeGradient is not enabled yet. If you are calling this from your own onEnable, "
                            + "add DeluxeGradient to your plugin.yml softdepend so it loads first, "
                            + "or use getOrNull() and retry later.");
        }
        return current;
    }

    /**
     * @return the API, or {@code null} if DeluxeGradient is not enabled
     */
    public static @Nullable DeluxeGradientApi getOrNull() {
        return instance;
    }

    public static boolean isAvailable() {
        return instance != null;
    }

    /**
     * Internal. Called by the plugin on enable and disable.
     */
    public static void set(final @Nullable DeluxeGradientApi api) {
        instance = api;
    }
}
