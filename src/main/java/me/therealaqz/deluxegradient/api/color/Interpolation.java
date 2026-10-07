// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.color;

/**
 * The colour spaces a gradient can be interpolated through.
 *
 * <p>The space matters more than people expect. Blending {@code #FF0000} to
 * {@code #0000FF} by averaging the raw channel bytes runs the midpoint through
 * a dark muddy purple, because sRGB channel values are gamma encoded and are
 * not linear in either light or perception. Every space below fixes a different
 * part of that problem.
 *
 * <h2>Why every space is split in two</h2>
 *
 * <p>Blending is expressed as {@link #decompose} plus {@link #recompose} rather
 * than as one {@code blend} method, because the expensive half depends only on
 * the stop colours. Converting {@code #FF0000} into OKLab costs three
 * {@link Math#cbrt} calls and three {@link Math#pow} calls, and the stops of a
 * gradient do not change after it is built. Splitting the conversion out lets
 * {@link Gradient} do it once at build time and leaves the per character work
 * as three multiply-adds against a {@code double[]} it already owns.
 *
 * <p>Doing it the obvious way instead, converting both stops inside a single
 * {@code blend} call, repeats that arithmetic and allocates two arrays for
 * every character of every chat message on the server. {@link #blend} is still
 * here for callers outside the render path, and is written in terms of the same
 * two halves so there is only one copy of each space's maths.
 */
public enum Interpolation {

    /**
     * Naive per channel blend of the gamma encoded sRGB bytes.
     *
     * <p>Technically wrong, and kept because it is what almost every other
     * gradient plugin does. Configs migrated from elsewhere should use this so
     * the output does not visibly change under the server owner.
     */
    SRGB {
        @Override
        void decompose(final int rgb, final double[] out, final int at) {
            out[at] = rgb >> 16 & 0xFF;
            out[at + 1] = rgb >> 8 & 0xFF;
            out[at + 2] = rgb & 0xFF;
        }

        @Override
        int recompose(final double[] stops, final int from, final int to, final double t) {
            return lerpByte(stops[from], stops[to], t) << 16
                    | lerpByte(stops[from + 1], stops[to + 1], t) << 8
                    | lerpByte(stops[from + 2], stops[to + 2], t);
        }
    },

    /**
     * Blend in linear light, then re-encode.
     *
     * <p>Physically correct in the sense that it matches what happens when you
     * fade one light source into another. Brighter through the middle than
     * {@link #SRGB}.
     */
    LINEAR_RGB {
        @Override
        void decompose(final int rgb, final double[] out, final int at) {
            out[at] = toLinear((rgb >> 16 & 0xFF) / 255.0);
            out[at + 1] = toLinear((rgb >> 8 & 0xFF) / 255.0);
            out[at + 2] = toLinear((rgb & 0xFF) / 255.0);
        }

        @Override
        int recompose(final double[] stops, final int from, final int to, final double t) {
            return pack(
                    toSrgb(lerp(stops[from], stops[to], t)),
                    toSrgb(lerp(stops[from + 1], stops[to + 1], t)),
                    toSrgb(lerp(stops[from + 2], stops[to + 2], t))
            );
        }
    },

    /**
     * Rotate hue the short way around the wheel, blending saturation and value
     * linearly.
     *
     * <p>Keeps colours vivid rather than passing through grey, which is what
     * you usually want between two saturated colours.
     */
    HSV {
        @Override
        void decompose(final int rgb, final double[] out, final int at) {
            toHsv(rgb, out, at);
        }

        @Override
        int recompose(final double[] stops, final int from, final int to, final double t) {
            return recomposeHsv(stops, from, to, t, false);
        }
    },

    /**
     * Rotate hue the long way around the wheel.
     *
     * <p>Two similar colours become a full spectrum sweep. This is how you get
     * a rainbow out of a two stop gradient.
     */
    HSV_LONG {
        @Override
        void decompose(final int rgb, final double[] out, final int at) {
            toHsv(rgb, out, at);
        }

        @Override
        int recompose(final double[] stops, final int from, final int to, final double t) {
            return recomposeHsv(stops, from, to, t, true);
        }
    },

    /**
     * Blend in OKLab, a perceptually uniform space.
     *
     * <p>Equal steps of {@code t} look like equal steps of colour change to a
     * human eye, and the midpoint between complementary colours stays bright
     * instead of going muddy. This is the recommended choice for new configs.
     */
    OKLAB {
        @Override
        void decompose(final int rgb, final double[] out, final int at) {
            toOkLab(rgb, out, at);
        }

        @Override
        int recompose(final double[] stops, final int from, final int to, final double t) {
            return fromOkLab(
                    lerp(stops[from], stops[to], t),
                    lerp(stops[from + 1], stops[to + 1], t),
                    lerp(stops[from + 2], stops[to + 2], t)
            );
        }
    };

    /** How many {@code double} slots one colour occupies in a decomposed array. */
    static final int COMPONENTS = 3;

    /**
     * Blends two packed 24 bit colours.
     *
     * <p>Convenience for callers holding two colours and nothing else. The
     * render path does not use this: it decomposes the stops once when the
     * gradient is built and calls {@link #recompose} per character. See the
     * class javadoc.
     *
     * @param t position between the two colours. Values outside {@code [0, 1]}
     *          are clamped, so callers do not have to defend against rounding
     *          drift when walking a long string.
     */
    public final int blend(final int from, final int to, final double t) {
        final double[] pair = new double[COMPONENTS * 2];
        decompose(from, pair, 0);
        decompose(to, pair, COMPONENTS);
        return recompose(pair, 0, COMPONENTS, t);
    }

    /**
     * Writes {@code rgb} into this space, as {@link #COMPONENTS} doubles
     * starting at {@code at}.
     *
     * <p>Takes the destination array rather than returning one so a caller
     * decomposing several stops can pack them all into a single array, and so
     * that nothing is allocated per call. Everything expensive about a colour
     * space conversion happens here.
     */
    abstract void decompose(int rgb, double[] out, int at);

    /**
     * Interpolates between two already decomposed colours and packs the result
     * back to 24 bit sRGB.
     *
     * <p>Both offsets index the same array, because the caller that matters
     * holds every stop of a gradient in one. Allocation free, and this is the
     * method that runs once per character.
     *
     * @param t position between the two colours, clamped to {@code [0, 1]}
     */
    abstract int recompose(double[] stops, int from, int to, double t);

    // ------------------------------------------------------------------
    // Shared maths
    // ------------------------------------------------------------------

    static double lerp(final double from, final double to, final double t) {
        final double clamped = t < 0.0 ? 0.0 : (t > 1.0 ? 1.0 : t);
        return from + ((to - from) * clamped);
    }

    private static int lerpByte(final double from, final double to, final double t) {
        return (int) Math.round(lerp(from, to, t));
    }

    private static int pack(final double r, final double g, final double b) {
        return clamp255(r) << 16 | clamp255(g) << 8 | clamp255(b);
    }

    private static int clamp255(final double channel) {
        final long scaled = Math.round(channel * 255.0);
        if (scaled < 0L) {
            return 0;
        }
        return scaled > 255L ? 255 : (int) scaled;
    }

    /**
     * sRGB transfer function, inverse. Maps an encoded channel to linear light.
     */
    private static double toLinear(final double channel) {
        return channel <= 0.04045
                ? channel / 12.92
                : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    /**
     * sRGB transfer function. Maps linear light back to an encoded channel.
     */
    private static double toSrgb(final double linear) {
        return linear <= 0.0031308
                ? linear * 12.92
                : (1.055 * Math.pow(linear, 1.0 / 2.4)) - 0.055;
    }

    private static int recomposeHsv(
            final double[] stops,
            final int from,
            final int to,
            final double t,
            final boolean longPath
    ) {
        double delta = stops[to] - stops[from];
        // Normalise into (-0.5, 0.5] so the default is the short way round.
        if (delta > 0.5) {
            delta -= 1.0;
        } else if (delta < -0.5) {
            delta += 1.0;
        }
        if (longPath) {
            // Take the complementary arc instead, preserving direction.
            delta += delta >= 0.0 ? -1.0 : 1.0;
        }

        final double clamped = t < 0.0 ? 0.0 : (t > 1.0 ? 1.0 : t);
        double hue = (stops[from] + (delta * clamped)) % 1.0;
        if (hue < 0.0) {
            hue += 1.0;
        }

        return fromHsv(
                hue,
                lerp(stops[from + 1], stops[to + 1], t),
                lerp(stops[from + 2], stops[to + 2], t)
        );
    }

    /**
     * Writes a packed colour as {@code [hue, saturation, value]}, all in
     * {@code [0, 1]}. Hand rolled rather than delegating to
     * {@code java.awt.Color} so the API module needs nothing outside
     * {@code java.base}, which keeps it usable on trimmed runtimes.
     */
    private static void toHsv(final int rgb, final double[] out, final int at) {
        final double r = (rgb >> 16 & 0xFF) / 255.0;
        final double g = (rgb >> 8 & 0xFF) / 255.0;
        final double b = (rgb & 0xFF) / 255.0;

        final double max = Math.max(r, Math.max(g, b));
        final double min = Math.min(r, Math.min(g, b));
        final double chroma = max - min;

        double hue = 0.0;
        if (chroma > 0.0) {
            if (max == r) {
                hue = ((g - b) / chroma) / 6.0;
            } else if (max == g) {
                hue = (2.0 + ((b - r) / chroma)) / 6.0;
            } else {
                hue = (4.0 + ((r - g) / chroma)) / 6.0;
            }
            if (hue < 0.0) {
                hue += 1.0;
            }
        }

        out[at] = hue;
        out[at + 1] = max == 0.0 ? 0.0 : chroma / max;
        out[at + 2] = max;
    }

    private static int fromHsv(final double hue, final double saturation, final double value) {
        final double sector = (hue - Math.floor(hue)) * 6.0;
        final int index = (int) sector;
        final double fraction = sector - index;

        final double p = value * (1.0 - saturation);
        final double q = value * (1.0 - (saturation * fraction));
        final double u = value * (1.0 - (saturation * (1.0 - fraction)));

        switch (index) {
            case 0:
                return pack(value, u, p);
            case 1:
                return pack(q, value, p);
            case 2:
                return pack(p, value, u);
            case 3:
                return pack(p, q, value);
            case 4:
                return pack(u, p, value);
            default:
                return pack(value, p, q);
        }
    }

    // ------------------------------------------------------------------
    // OKLab, after Björn Ottosson's reference implementation.
    // Package visible because LegacyColor quantises in this space too.
    // ------------------------------------------------------------------

    /**
     * Allocating form, for the one caller that wants a colour on its own.
     * {@link LegacyColor} quantises against a fixed sixteen entry palette
     * rather than per character, so the array costs nothing that matters there.
     */
    static double[] toOkLab(final int rgb) {
        final double[] lab = new double[COMPONENTS];
        toOkLab(rgb, lab, 0);
        return lab;
    }

    static void toOkLab(final int rgb, final double[] out, final int at) {
        final double r = toLinear((rgb >> 16 & 0xFF) / 255.0);
        final double g = toLinear((rgb >> 8 & 0xFF) / 255.0);
        final double b = toLinear((rgb & 0xFF) / 255.0);

        final double l = Math.cbrt((0.4122214708 * r) + (0.5363325363 * g) + (0.0514459929 * b));
        final double m = Math.cbrt((0.2119034982 * r) + (0.6806995451 * g) + (0.1073969566 * b));
        final double s = Math.cbrt((0.0883024619 * r) + (0.2817188376 * g) + (0.6299787005 * b));

        out[at] = (0.2104542553 * l) + (0.7936177850 * m) - (0.0040720468 * s);
        out[at + 1] = (1.9779984951 * l) - (2.4285922050 * m) + (0.4505937099 * s);
        out[at + 2] = (0.0259040371 * l) + (0.7827717662 * m) - (0.8086757660 * s);
    }

    static int fromOkLab(final double lightness, final double a, final double b) {
        double l = lightness + (0.3963377774 * a) + (0.2158037573 * b);
        double m = lightness - (0.1055613458 * a) - (0.0638541728 * b);
        double s = lightness - (0.0894841775 * a) - (1.2914855480 * b);

        l = l * l * l;
        m = m * m * m;
        s = s * s * s;

        return pack(
                toSrgb((4.0767416621 * l) - (3.3077115913 * m) + (0.2309699292 * s)),
                toSrgb((-1.2684380046 * l) + (2.6097574011 * m) - (0.3413193965 * s)),
                toSrgb((-0.0041960863 * l) - (0.7034186147 * m) + (1.7076147010 * s))
        );
    }
}
