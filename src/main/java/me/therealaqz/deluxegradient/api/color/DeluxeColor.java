// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable 24 bit colour.
 *
 * <p>Deliberately not {@code java.awt.Color}: that class drags in the
 * {@code java.desktop} module, carries an alpha channel Minecraft cannot use,
 * and is mutable in ways that have surprised people for thirty years.
 */
public final class DeluxeColor {

    public static final DeluxeColor WHITE = new DeluxeColor(0xFFFFFF);
    public static final DeluxeColor BLACK = new DeluxeColor(0x000000);

    private final int rgb;

    private DeluxeColor(final int rgb) {
        this.rgb = rgb & 0xFFFFFF;
    }

    // ------------------------------------------------------------------
    // Factories
    // ------------------------------------------------------------------

    public static @NotNull DeluxeColor of(final int rgb) {
        return new DeluxeColor(rgb);
    }

    public static @NotNull DeluxeColor of(final int red, final int green, final int blue) {
        return new DeluxeColor(clamp(red) << 16 | clamp(green) << 8 | clamp(blue));
    }

    public static @NotNull DeluxeColor of(final @NotNull LegacyColor legacy) {
        return new DeluxeColor(legacy.rgb());
    }

    /**
     * Parses a hex colour.
     *
     * <p>Accepts {@code #RRGGBB}, {@code RRGGBB}, {@code #RGB} and {@code RGB}.
     * The three digit form expands each digit, so {@code #f0a} is
     * {@code #FF00AA}, matching CSS.
     *
     * @return the colour, or {@code null} if the input is not a valid hex
     *         colour. Returning null rather than throwing is deliberate: the
     *         lexer tries several interpretations of a token and needs a cheap
     *         way to reject one.
     */
    public static @Nullable DeluxeColor fromHex(final @Nullable String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        final String digits = input.charAt(0) == '#' ? input.substring(1) : input;

        if (digits.length() == 6) {
            final int value = parseHex(digits);
            return value < 0 ? null : new DeluxeColor(value);
        }
        if (digits.length() == 3) {
            final int value = parseHex(digits);
            if (value < 0) {
                return null;
            }
            final int r = value >> 8 & 0xF;
            final int g = value >> 4 & 0xF;
            final int b = value & 0xF;
            return new DeluxeColor(r * 0x11 << 16 | g * 0x11 << 8 | b * 0x11);
        }
        return null;
    }

    /**
     * Parses a hex colour or a colour name.
     *
     * <p>This is the entry point the lexer uses, so it accepts everything a
     * server owner might reasonably type: {@code #ff0000}, {@code ff0000},
     * {@code red}, {@code dark_red}, {@code darkred}, {@code DARK RED}.
     *
     * @return the colour, or {@code null} if nothing matched.
     */
    public static @Nullable DeluxeColor parse(final @Nullable String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        final DeluxeColor hex = fromHex(input);
        if (hex != null) {
            return hex;
        }
        return NamedColors.lookup(input);
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public int rgb() {
        return this.rgb;
    }

    public int red() {
        return this.rgb >> 16 & 0xFF;
    }

    public int green() {
        return this.rgb >> 8 & 0xFF;
    }

    public int blue() {
        return this.rgb & 0xFF;
    }

    /**
     * The uppercase {@code #RRGGBB} form.
     */
    public @NotNull String toHexString() {
        final char[] out = new char[7];
        out[0] = '#';
        for (int i = 0; i < 6; i++) {
            out[6 - i] = HEX_DIGITS[this.rgb >> (i * 4) & 0xF];
        }
        return new String(out);
    }

    /**
     * The legacy section sign form a 1.16 or newer client understands:
     * {@code §x§R§R§G§G§B§B}.
     *
     * <p>Fourteen characters for one colour. That cost is why anything with a
     * character budget, such as a scoreboard team prefix, has to be measured
     * after rendering rather than before.
     */
    public @NotNull String toLegacyString() {
        final char[] out = new char[14];
        out[0] = SECTION;
        out[1] = 'x';
        for (int i = 0; i < 6; i++) {
            out[2 + (i * 2)] = SECTION;
            out[3 + (i * 2)] = HEX_DIGITS[this.rgb >> ((5 - i) * 4) & 0xF];
        }
        return new String(out);
    }

    /**
     * The closest of the sixteen pre-1.16 colours, measured perceptually.
     */
    public @NotNull LegacyColor nearestLegacy() {
        return LegacyColor.nearest(this.rgb);
    }

    // ------------------------------------------------------------------

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof DeluxeColor && ((DeluxeColor) other).rgb == this.rgb;
    }

    @Override
    public int hashCode() {
        return this.rgb;
    }

    @Override
    public String toString() {
        return toHexString();
    }

    // ------------------------------------------------------------------

    private static final char SECTION = '§';
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private static int clamp(final int channel) {
        if (channel < 0) {
            return 0;
        }
        return channel > 255 ? 255 : channel;
    }

    /**
     * Strict hex parse. {@link Integer#parseInt(String, int)} is not usable
     * here because it accepts a leading {@code +} or {@code -}, so "-ff000"
     * would parse as a colour.
     */
    private static int parseHex(final String digits) {
        int value = 0;
        for (int i = 0; i < digits.length(); i++) {
            final int digit = Character.digit(digits.charAt(i), 16);
            if (digit < 0) {
                return -1;
            }
            value = value << 4 | digit;
        }
        return value;
    }
}
