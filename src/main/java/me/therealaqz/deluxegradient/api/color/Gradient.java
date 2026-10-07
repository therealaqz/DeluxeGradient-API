// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.color;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An immutable, resolved colour ramp.
 *
 * <p>Construction does all the work that does not depend on the text: stop
 * positions are resolved and sorted, the colours are unpacked into a primitive
 * array, and each stop is converted once into the interpolation colour space.
 * That last step is what keeps {@link #rgbAt(double)} allocation free and free
 * of {@code cbrt} and {@code pow} calls, which matters because it runs once per
 * character of every chat message on the server.
 *
 * <p>{@link #colorAt(double)} is the same sample boxed into a
 * {@link DeluxeColor}, so it does allocate. Renderers call {@code rgbAt}.
 *
 * <p>Instances are safe to share across threads and are intended to be built
 * once at config load and cached.
 */
public final class Gradient {

    /** What happens when a sampling position falls outside {@code [0, 1]}. */
    public enum Edge {
        /** Hold the end colour. The sane default for a normal gradient. */
        CLAMP,
        /** Wrap around to the start. Produces a seamless repeating ramp. */
        WRAP,
        /** Reflect back the way it came. Repeats without a seam at the join. */
        MIRROR
    }

    private final int[] colors;
    private final double[] positions;
    /**
     * Every stop converted into {@link #interpolation}'s colour space, three
     * doubles per stop, laid out end to end. Built once here so the sampler
     * never converts a stop again. See {@link Interpolation#decompose}.
     */
    private final double[] components;
    private final Interpolation interpolation;
    private final WaveSpec wave;
    private final double cycles;
    private final Edge edge;
    /**
     * Packed {@code me.therealaqz.deluxegradient.api.text.Decoration} bits that
     * every character of this gradient wears.
     *
     * <p>Held as a bare int rather than a set of that enum on purpose. A
     * gradient is a colour ramp, and the colour package has no business
     * depending on the text package to carry four bits. The renderer is the
     * only thing that has to know what the bits mean, and it already does.
     *
     * <p>It lives here rather than beside the selection because
     * {@link Gradient} is the single value that reaches every render site:
     * chat, names, placeholders, menu previews and the public API all receive
     * one and nothing else. Threading a second value alongside it would mean
     * a decoration that works in one of those places and silently does not in
     * the rest.
     */
    private final int decorations;

    private Gradient(
            final int[] colors,
            final double[] positions,
            final double[] components,
            final Interpolation interpolation,
            final WaveSpec wave,
            final double cycles,
            final Edge edge,
            final int decorations
    ) {
        this.colors = colors;
        this.positions = positions;
        this.components = components;
        this.interpolation = interpolation;
        this.wave = wave;
        this.cycles = cycles;
        this.edge = edge;
        this.decorations = decorations;
    }

    /**
     * Converts every stop into {@code interpolation}'s space.
     *
     * <p>Split out because {@link #withPhase(double)} reuses the result: a
     * phase change moves where the ramp is sampled and never what the stops
     * are, so an animated surface deriving a gradient per tick must not redo
     * this.
     */
    private static double[] decomposeStops(final int[] colors, final Interpolation interpolation) {
        final double[] components = new double[colors.length * Interpolation.COMPONENTS];
        for (int i = 0; i < colors.length; i++) {
            interpolation.decompose(colors[i], components, i * Interpolation.COMPONENTS);
        }
        return components;
    }

    // ------------------------------------------------------------------
    // Factories
    // ------------------------------------------------------------------

    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * The common two or more stop gradient: evenly spaced, OKLab, no wave.
     */
    public static @NotNull Gradient of(final @NotNull DeluxeColor... colors) {
        final Builder builder = builder();
        for (final DeluxeColor color : colors) {
            builder.stop(ColorStop.of(color));
        }
        return builder.build();
    }

    /**
     * A full hue sweep.
     *
     * <p>Built from six evenly spaced stops around the wheel rather than two
     * stops with a long hue path, because six stops behave predictably when a
     * wave is layered on top and two stops do not.
     */
    public static @NotNull Gradient rainbow() {
        return builder()
                .stop(ColorStop.of(DeluxeColor.of(0xFF0000)))
                .stop(ColorStop.of(DeluxeColor.of(0xFFFF00)))
                .stop(ColorStop.of(DeluxeColor.of(0x00FF00)))
                .stop(ColorStop.of(DeluxeColor.of(0x00FFFF)))
                .stop(ColorStop.of(DeluxeColor.of(0x0000FF)))
                .stop(ColorStop.of(DeluxeColor.of(0xFF00FF)))
                .stop(ColorStop.of(DeluxeColor.of(0xFF0000)))
                .interpolation(Interpolation.HSV)
                .edge(Edge.WRAP)
                .build();
    }

    // ------------------------------------------------------------------
    // Sampling
    // ------------------------------------------------------------------

    /**
     * Samples the ramp.
     *
     * @param t position along the gradient. Values outside {@code [0, 1]} are
     *          resolved according to {@link Edge}.
     */
    public @NotNull DeluxeColor colorAt(final double t) {
        return DeluxeColor.of(rgbAt(t));
    }

    /**
     * Samples the ramp, returning the packed value directly.
     *
     * <p>Renderers use this to avoid allocating a {@link DeluxeColor} per
     * character.
     */
    public int rgbAt(final double t) {
        double u = this.wave.apply(t) * this.cycles;
        u = resolveEdge(u);

        // Linear scan. Gradients have a handful of stops, so a binary search
        // would cost more in branch misprediction than it saves.
        final int last = this.positions.length - 1;
        if (u <= this.positions[0]) {
            return this.colors[0];
        }
        if (u >= this.positions[last]) {
            return this.colors[last];
        }
        for (int i = 0; i < last; i++) {
            final double from = this.positions[i];
            final double to = this.positions[i + 1];
            if (u <= to) {
                final double span = to - from;
                final double local = span <= 0.0 ? 0.0 : (u - from) / span;
                // Not blend(int, int, double): the stops are already in the
                // interpolation space, and re-deriving them here is what makes
                // a long message expensive.
                return this.interpolation.recompose(
                        this.components,
                        i * Interpolation.COMPONENTS,
                        (i + 1) * Interpolation.COMPONENTS,
                        local);
            }
        }
        return this.colors[last];
    }

    /**
     * Samples for character {@code index} of a run {@code length} characters
     * long.
     *
     * <p>A single character run samples the start of the ramp rather than
     * dividing by zero, and a two character run gets the two endpoints.
     */
    public int rgbAtIndex(final int index, final int length) {
        if (length <= 1) {
            return rgbAt(0.0);
        }
        return rgbAt((double) index / (double) (length - 1));
    }

    private double resolveEdge(final double u) {
        if (u >= 0.0 && u <= 1.0) {
            return u;
        }
        switch (this.edge) {
            case WRAP: {
                final double wrapped = u - Math.floor(u);
                return wrapped;
            }
            case MIRROR: {
                final double period = Math.abs(u) % 2.0;
                return period > 1.0 ? 2.0 - period : period;
            }
            case CLAMP:
            default:
                return u < 0.0 ? 0.0 : 1.0;
        }
    }

    // ------------------------------------------------------------------
    // Derivation
    // ------------------------------------------------------------------

    /**
     * Returns a copy with the wave phase advanced. Used per tick by animated
     * surfaces; the original stays immutable and shareable.
     */
    public @NotNull Gradient withPhase(final double phase) {
        if (this.wave.isNone()) {
            return this;
        }
        return new Gradient(this.colors, this.positions, this.components, this.interpolation,
                this.wave.withPhase(phase), this.cycles, this.edge, this.decorations);
    }

    public boolean isAnimatable() {
        return !this.wave.isNone();
    }

    public @NotNull Interpolation interpolation() {
        return this.interpolation;
    }

    public @NotNull WaveSpec wave() {
        return this.wave;
    }

    public int stopCount() {
        return this.colors.length;
    }

    /**
     * The packed decoration bits, or {@code 0} for undecorated text.
     *
     * <p>Bits are the ones {@code Decoration.mask()} hands out.
     */
    public int decorations() {
        return this.decorations;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder("Gradient[");
        for (int i = 0; i < this.colors.length; i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(DeluxeColor.of(this.colors[i])).append('@').append(this.positions[i]);
        }
        return out.append(", ").append(this.interpolation)
                .append(this.wave.isNone() ? "" : ", " + this.wave)
                .append(']').toString();
    }

    // ------------------------------------------------------------------

    public static final class Builder {

        private final List<ColorStop> stops = new ArrayList<ColorStop>(4);
        private Interpolation interpolation = Interpolation.OKLAB;
        private WaveSpec wave = WaveSpec.NONE;
        private double cycles = 1.0;
        private Edge edge = Edge.CLAMP;
        private int decorations;

        private Builder() {
        }

        public @NotNull Builder stop(final @NotNull ColorStop stop) {
            this.stops.add(stop);
            return this;
        }

        public @NotNull Builder stops(final @NotNull List<ColorStop> newStops) {
            this.stops.addAll(newStops);
            return this;
        }

        public @NotNull Builder interpolation(final @NotNull Interpolation newInterpolation) {
            this.interpolation = newInterpolation;
            return this;
        }

        public @NotNull Builder wave(final @NotNull WaveSpec newWave) {
            this.wave = newWave;
            return this;
        }

        /**
         * How many times the ramp repeats across the text. Values other than
         * {@code 1} only make sense with {@link Edge#WRAP} or
         * {@link Edge#MIRROR}.
         */
        public @NotNull Builder cycles(final double newCycles) {
            this.cycles = newCycles <= 0.0 ? 1.0 : newCycles;
            return this;
        }

        public @NotNull Builder edge(final @NotNull Edge newEdge) {
            this.edge = newEdge;
            return this;
        }

        /**
         * Decorations every character wears, as packed
         * {@code Decoration.mask()} bits.
         */
        public @NotNull Builder decorations(final int mask) {
            this.decorations = mask;
            return this;
        }

        public @NotNull Gradient build() {
            if (this.stops.isEmpty()) {
                throw new IllegalStateException("A gradient needs at least one colour stop.");
            }

            final List<ColorStop> resolved = resolvePositions(this.stops);
            Collections.sort(resolved, new Comparator<ColorStop>() {
                @Override
                public int compare(final ColorStop a, final ColorStop b) {
                    return Double.compare(a.position(), b.position());
                }
            });

            final int[] colors = new int[resolved.size()];
            final double[] positions = new double[resolved.size()];
            for (int i = 0; i < resolved.size(); i++) {
                colors[i] = resolved.get(i).color().rgb();
                positions[i] = resolved.get(i).position();
            }

            return new Gradient(colors, positions, decomposeStops(colors, this.interpolation),
                    this.interpolation, this.wave, this.cycles, this.edge, this.decorations);
        }

        /**
         * Assigns a position to every {@link ColorStop#AUTO} stop.
         *
         * <p>Explicitly positioned stops act as anchors, and each run of
         * automatic stops between two anchors is spread evenly across the gap.
         * A leading or trailing run is anchored at 0 or 1 respectively, so the
         * all-automatic case reduces to plain even spacing.
         */
        private static List<ColorStop> resolvePositions(final List<ColorStop> input) {
            final int size = input.size();
            if (size == 1) {
                final ColorStop only = input.get(0);
                return new ArrayList<ColorStop>(
                        Arrays.asList(only.isAutoPositioned() ? only.withPosition(0.0) : only));
            }

            final ColorStop[] out = input.toArray(new ColorStop[0]);

            int index = 0;
            while (index < size) {
                if (!out[index].isAutoPositioned()) {
                    index++;
                    continue;
                }

                int runEnd = index;
                while (runEnd < size && out[runEnd].isAutoPositioned()) {
                    runEnd++;
                }

                final double before = index == 0 ? 0.0 : out[index - 1].position();
                final double after = runEnd == size ? 1.0 : out[runEnd].position();

                // Number of gaps to divide the span into. A leading run starts
                // exactly at `before`; an interior run starts one step past it.
                final int leadingOffset = index == 0 ? 0 : 1;
                final int trailingOffset = runEnd == size ? 0 : 1;
                final int steps = (runEnd - index) - 1 + leadingOffset + trailingOffset;
                final double span = after - before;

                for (int i = index; i < runEnd; i++) {
                    final double fraction = steps <= 0
                            ? 0.0
                            : (double) ((i - index) + leadingOffset) / (double) steps;
                    out[i] = out[i].withPosition(before + (span * fraction));
                }

                index = runEnd;
            }

            return new ArrayList<ColorStop>(Arrays.asList(out));
        }
    }
}
