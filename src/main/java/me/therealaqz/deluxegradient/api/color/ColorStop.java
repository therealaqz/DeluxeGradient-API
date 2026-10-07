// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.color;

import org.jetbrains.annotations.NotNull;

/**
 * One colour pinned to a position along a gradient.
 *
 * <p>Position is in {@code [0, 1]}. A stop with no explicit position is spaced
 * evenly by {@link Gradient}, which is what makes the common
 * {@code <gradient:#f00:#0f0:#00f>} form work without anyone having to think
 * about fractions.
 */
public final class ColorStop {

    /** Sentinel meaning "space me evenly with the other unpositioned stops". */
    public static final double AUTO = -1.0;

    private final DeluxeColor color;
    private final double position;

    private ColorStop(final DeluxeColor color, final double position) {
        this.color = color;
        this.position = position;
    }

    public static @NotNull ColorStop of(final @NotNull DeluxeColor color) {
        return new ColorStop(color, AUTO);
    }

    public static @NotNull ColorStop at(final @NotNull DeluxeColor color, final double position) {
        return new ColorStop(color, clamp(position));
    }

    public @NotNull DeluxeColor color() {
        return this.color;
    }

    public double position() {
        return this.position;
    }

    public boolean isAutoPositioned() {
        return this.position == AUTO;
    }

    @NotNull ColorStop withPosition(final double resolved) {
        return new ColorStop(this.color, clamp(resolved));
    }

    private static double clamp(final double value) {
        if (value < 0.0) {
            return 0.0;
        }
        return value > 1.0 ? 1.0 : value;
    }

    @Override
    public String toString() {
        return this.color + "@" + (isAutoPositioned() ? "auto" : this.position);
    }
}
