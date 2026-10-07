// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.color;

/**
 * The sixteen colours Minecraft supported before 1.16.
 *
 * <p>These are still needed on modern servers. Some surfaces are string typed
 * and length limited, and quantising to a legacy code costs two characters
 * where a hex colour costs fourteen. They are also the only thing a client
 * below 1.16 understands, which is what makes best effort rendering on those
 * versions possible at all.
 *
 * <p>The RGB values are the ones the vanilla client actually renders, taken
 * from the chat colour table. They are not the "obvious" values: {@code GREEN}
 * is {@code 55FF55} rather than {@code 00FF00}.
 */
public enum LegacyColor {

    BLACK('0', 0x000000),
    DARK_BLUE('1', 0x0000AA),
    DARK_GREEN('2', 0x00AA00),
    DARK_AQUA('3', 0x00AAAA),
    DARK_RED('4', 0xAA0000),
    DARK_PURPLE('5', 0xAA00AA),
    GOLD('6', 0xFFAA00),
    GRAY('7', 0xAAAAAA),
    DARK_GRAY('8', 0x555555),
    BLUE('9', 0x5555FF),
    GREEN('a', 0x55FF55),
    AQUA('b', 0x55FFFF),
    RED('c', 0xFF5555),
    LIGHT_PURPLE('d', 0xFF55FF),
    YELLOW('e', 0xFFFF55),
    WHITE('f', 0xFFFFFF);

    private static final LegacyColor[] VALUES = values();

    /** See {@link #nearest(int)} for why this is not 1. */
    private static final double CHROMA_WEIGHT = 2.0;

    private final char code;
    private final int rgb;

    LegacyColor(final char code, final int rgb) {
        this.code = code;
        this.rgb = rgb;
    }

    /**
     * The character that follows the section sign, always lower case.
     */
    public char code() {
        return this.code;
    }

    /**
     * The packed 24 bit colour this code renders as on a vanilla client.
     */
    public int rgb() {
        return this.rgb;
    }

    /**
     * Looks up a colour by its code character, accepting either case.
     *
     * @return the matching colour, or {@code null} if the character is not one
     *         of the sixteen colour codes. Format codes such as {@code l} and
     *         {@code r} return {@code null}; they are handled as decorations.
     */
    public static LegacyColor byCode(final char code) {
        final char lower = Character.toLowerCase(code);
        for (final LegacyColor colour : VALUES) {
            if (colour.code == lower) {
                return colour;
            }
        }
        return null;
    }

    /**
     * Finds the legacy colour that looks closest to an arbitrary RGB value.
     *
     * <p>Distance is measured in OKLab rather than RGB. Euclidean distance in
     * RGB is a poor model of perceived difference, and using it here produces
     * the classic result where a mid grey quantises to dark blue because the
     * numbers happen to be close.
     *
     * <p>The two chroma axes are weighted above lightness, which is not what a
     * plain OKLab colour difference does. With only sixteen colours to choose
     * from, no candidate is close, and the question becomes which kind of error
     * is less objectionable. Getting the hue wrong is far more visible than
     * getting the brightness wrong: unweighted, {@code #808080} matches dark
     * aqua rather than grey, because a small lightness gap outscores a large
     * shift into cyan. Weighting chroma corrects that.
     */
    public static LegacyColor nearest(final int rgb) {
        final double[] target = Interpolation.toOkLab(rgb);

        LegacyColor best = WHITE;
        double bestDistance = Double.MAX_VALUE;

        for (final LegacyColor candidate : VALUES) {
            final double[] lab = Interpolation.toOkLab(candidate.rgb);
            final double dl = lab[0] - target[0];
            final double da = (lab[1] - target[1]) * CHROMA_WEIGHT;
            final double db = (lab[2] - target[2]) * CHROMA_WEIGHT;
            final double distance = (dl * dl) + (da * da) + (db * db);

            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }
}
