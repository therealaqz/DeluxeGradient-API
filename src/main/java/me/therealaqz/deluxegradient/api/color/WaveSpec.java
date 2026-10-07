// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.color;

import org.jetbrains.annotations.NotNull;

/**
 * A sinusoidal distortion applied to a gradient's sampling position.
 *
 * <p>This class describes the <em>spatial</em> wave: the colour oscillates along
 * the string, and the result is computed once at render time. It works
 * everywhere, including chat, because it needs nothing more than the text
 * itself.
 *
 * <p>A <em>moving</em> wave is this same spec with {@link #phase()} advanced
 * on a tick and the surface re-rendered. The plugin does that for the tab list
 * only; a placeholder consumer that re-reads the name moves at its own refresh
 * rate. A chat message cannot be recoloured after it is sent, so in chat the
 * wave is always drawn still.
 *
 * <p>
 */
public final class WaveSpec {

    /** No distortion. Sampling position passes through untouched. */
    public static final WaveSpec NONE = new WaveSpec(0.0, 0.0, 0.0);

    private final double amplitude;
    private final double frequency;
    private final double phase;

    private WaveSpec(final double amplitude, final double frequency, final double phase) {
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
    }

    /**
     * @param amplitude how far the sampling position is pushed, as a fraction
     *                  of the gradient's length. Clamped to {@code [0, 1]}.
     *                  Around {@code 0.15} reads as a gentle shimmer; {@code 1}
     *                  makes the gradient fold back over itself.
     * @param frequency how many complete oscillations occur across the text.
     * @param phase     starting offset in turns, where {@code 1} is a full
     *                  cycle. Advancing this over time is what animates.
     */
    public static @NotNull WaveSpec of(final double amplitude, final double frequency, final double phase) {
        if (amplitude <= 0.0 || frequency == 0.0) {
            return NONE;
        }
        return new WaveSpec(Math.min(amplitude, 1.0), frequency, phase);
    }

    public double amplitude() {
        return this.amplitude;
    }

    public double frequency() {
        return this.frequency;
    }

    public double phase() {
        return this.phase;
    }

    public boolean isNone() {
        return this == NONE || this.amplitude <= 0.0;
    }

    /**
     * Returns a copy at a new phase. Used by the animation scheduler, which
     * keeps the spec immutable and derives a fresh one per tick rather than
     * mutating shared state across threads.
     */
    public @NotNull WaveSpec withPhase(final double newPhase) {
        return isNone() ? NONE : new WaveSpec(this.amplitude, this.frequency, newPhase);
    }

    /**
     * Distorts a sampling position.
     *
     * <p>The result is deliberately not clamped here. {@link Gradient} decides
     * what happens past the ends, because whether the gradient clamps or wraps
     * is the gradient's business, not the wave's.
     */
    public double apply(final double t) {
        if (isNone()) {
            return t;
        }
        return t + (this.amplitude * Math.sin(2.0 * Math.PI * ((this.frequency * t) + this.phase)));
    }

    @Override
    public String toString() {
        return isNone()
                ? "WaveSpec.NONE"
                : "WaveSpec[amplitude=" + this.amplitude + ", frequency=" + this.frequency
                        + ", phase=" + this.phase + ']';
    }
}
