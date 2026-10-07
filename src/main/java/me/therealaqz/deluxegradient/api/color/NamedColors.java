// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Colour name resolution for the sixteen Minecraft names and the full CSS
 * named colour set.
 *
 * <p>Eight names exist in both sets with different values. CSS {@code red} is
 * {@code #FF0000}; Minecraft {@code red} is {@code #FF5555}. Where they
 * collide, <strong>Minecraft wins</strong>. That matches what MiniMessage does,
 * and it is what someone typing {@code <red>} into a Minecraft config means.
 *
 * <p>The CSS value is still reachable through the {@code css:} namespace, and
 * the Minecraft value can be forced with {@code mc:}:
 *
 * <pre>
 *   &lt;red&gt;        -&gt; #FF5555  (Minecraft)
 *   &lt;css:red&gt;    -&gt; #FF0000  (CSS)
 *   &lt;mc:red&gt;     -&gt; #FF5555  (explicit)
 * </pre>
 *
 * <p>Lookups are lenient about separators and case, so {@code dark_red},
 * {@code darkred}, {@code DARK RED} and {@code dark-red} are the same name.
 */
public final class NamedColors {

    private static final String CSS_NAMESPACE = "css:";
    private static final String MINECRAFT_NAMESPACE = "mc:";
    private static final String MINECRAFT_NAMESPACE_LONG = "minecraft:";

    private static final Map<String, DeluxeColor> MINECRAFT;
    private static final Map<String, DeluxeColor> CSS;

    private NamedColors() {
    }

    /**
     * Resolves a colour name.
     *
     * @param name the name, optionally namespaced with {@code css:} or
     *             {@code mc:}. Case and separators are ignored.
     * @return the colour, or {@code null} if the name is not known.
     */
    public static @Nullable DeluxeColor lookup(final @Nullable String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        final String trimmed = name.trim().toLowerCase(Locale.ROOT);

        if (trimmed.startsWith(CSS_NAMESPACE)) {
            return CSS.get(normalise(trimmed.substring(CSS_NAMESPACE.length())));
        }
        if (trimmed.startsWith(MINECRAFT_NAMESPACE)) {
            return MINECRAFT.get(normalise(trimmed.substring(MINECRAFT_NAMESPACE.length())));
        }
        if (trimmed.startsWith(MINECRAFT_NAMESPACE_LONG)) {
            return MINECRAFT.get(normalise(trimmed.substring(MINECRAFT_NAMESPACE_LONG.length())));
        }

        final String key = normalise(trimmed);
        final DeluxeColor minecraft = MINECRAFT.get(key);
        return minecraft != null ? minecraft : CSS.get(key);
    }

    /**
     * Every name this class understands, sorted, for tab completion and for the
     * colour picker menu.
     */
    public static @NotNull Set<String> names() {
        final Set<String> all = new TreeSet<String>(MINECRAFT.keySet());
        all.addAll(CSS.keySet());
        return Collections.unmodifiableSet(all);
    }

    /**
     * Strips everything that is only there for readability. This is what makes
     * {@code dark_red} and {@code DARK RED} the same key.
     */
    private static String normalise(final String input) {
        final StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c != '_' && c != '-' && c != ' ') {
                out.append(Character.toLowerCase(c));
            }
        }
        return out.toString();
    }

    private static void put(final Map<String, DeluxeColor> target, final String name, final int rgb) {
        target.put(normalise(name), DeluxeColor.of(rgb));
    }

    static {
        final Map<String, DeluxeColor> minecraft = new HashMap<String, DeluxeColor>(32);
        for (final LegacyColor colour : LegacyColor.values()) {
            put(minecraft, colour.name(), colour.rgb());
        }
        // Aliases the vanilla client and most plugins accept.
        put(minecraft, "grey", LegacyColor.GRAY.rgb());
        put(minecraft, "dark_grey", LegacyColor.DARK_GRAY.rgb());
        put(minecraft, "pink", LegacyColor.LIGHT_PURPLE.rgb());
        put(minecraft, "orange", LegacyColor.GOLD.rgb());
        MINECRAFT = Collections.unmodifiableMap(minecraft);

        final Map<String, DeluxeColor> css = new HashMap<String, DeluxeColor>(192);
        put(css, "aliceblue", 0xF0F8FF);
        put(css, "antiquewhite", 0xFAEBD7);
        put(css, "aqua", 0x00FFFF);
        put(css, "aquamarine", 0x7FFFD4);
        put(css, "azure", 0xF0FFFF);
        put(css, "beige", 0xF5F5DC);
        put(css, "bisque", 0xFFE4C4);
        put(css, "black", 0x000000);
        put(css, "blanchedalmond", 0xFFEBCD);
        put(css, "blue", 0x0000FF);
        put(css, "blueviolet", 0x8A2BE2);
        put(css, "brown", 0xA52A2A);
        put(css, "burlywood", 0xDEB887);
        put(css, "cadetblue", 0x5F9EA0);
        put(css, "chartreuse", 0x7FFF00);
        put(css, "chocolate", 0xD2691E);
        put(css, "coral", 0xFF7F50);
        put(css, "cornflowerblue", 0x6495ED);
        put(css, "cornsilk", 0xFFF8DC);
        put(css, "crimson", 0xDC143C);
        put(css, "cyan", 0x00FFFF);
        put(css, "darkblue", 0x00008B);
        put(css, "darkcyan", 0x008B8B);
        put(css, "darkgoldenrod", 0xB8860B);
        put(css, "darkgray", 0xA9A9A9);
        put(css, "darkgrey", 0xA9A9A9);
        put(css, "darkgreen", 0x006400);
        put(css, "darkkhaki", 0xBDB76B);
        put(css, "darkmagenta", 0x8B008B);
        put(css, "darkolivegreen", 0x556B2F);
        put(css, "darkorange", 0xFF8C00);
        put(css, "darkorchid", 0x9932CC);
        put(css, "darkred", 0x8B0000);
        put(css, "darksalmon", 0xE9967A);
        put(css, "darkseagreen", 0x8FBC8F);
        put(css, "darkslateblue", 0x483D8B);
        put(css, "darkslategray", 0x2F4F4F);
        put(css, "darkslategrey", 0x2F4F4F);
        put(css, "darkturquoise", 0x00CED1);
        put(css, "darkviolet", 0x9400D3);
        put(css, "deeppink", 0xFF1493);
        put(css, "deepskyblue", 0x00BFFF);
        put(css, "dimgray", 0x696969);
        put(css, "dimgrey", 0x696969);
        put(css, "dodgerblue", 0x1E90FF);
        put(css, "firebrick", 0xB22222);
        put(css, "floralwhite", 0xFFFAF0);
        put(css, "forestgreen", 0x228B22);
        put(css, "fuchsia", 0xFF00FF);
        put(css, "gainsboro", 0xDCDCDC);
        put(css, "ghostwhite", 0xF8F8FF);
        put(css, "gold", 0xFFD700);
        put(css, "goldenrod", 0xDAA520);
        put(css, "gray", 0x808080);
        put(css, "grey", 0x808080);
        put(css, "green", 0x008000);
        put(css, "greenyellow", 0xADFF2F);
        put(css, "honeydew", 0xF0FFF0);
        put(css, "hotpink", 0xFF69B4);
        put(css, "indianred", 0xCD5C5C);
        put(css, "indigo", 0x4B0082);
        put(css, "ivory", 0xFFFFF0);
        put(css, "khaki", 0xF0E68C);
        put(css, "lavender", 0xE6E6FA);
        put(css, "lavenderblush", 0xFFF0F5);
        put(css, "lawngreen", 0x7CFC00);
        put(css, "lemonchiffon", 0xFFFACD);
        put(css, "lightblue", 0xADD8E6);
        put(css, "lightcoral", 0xF08080);
        put(css, "lightcyan", 0xE0FFFF);
        put(css, "lightgoldenrodyellow", 0xFAFAD2);
        put(css, "lightgray", 0xD3D3D3);
        put(css, "lightgrey", 0xD3D3D3);
        put(css, "lightgreen", 0x90EE90);
        put(css, "lightpink", 0xFFB6C1);
        put(css, "lightsalmon", 0xFFA07A);
        put(css, "lightseagreen", 0x20B2AA);
        put(css, "lightskyblue", 0x87CEFA);
        put(css, "lightslategray", 0x778899);
        put(css, "lightslategrey", 0x778899);
        put(css, "lightsteelblue", 0xB0C4DE);
        put(css, "lightyellow", 0xFFFFE0);
        put(css, "lime", 0x00FF00);
        put(css, "limegreen", 0x32CD32);
        put(css, "linen", 0xFAF0E6);
        put(css, "magenta", 0xFF00FF);
        put(css, "maroon", 0x800000);
        put(css, "mediumaquamarine", 0x66CDAA);
        put(css, "mediumblue", 0x0000CD);
        put(css, "mediumorchid", 0xBA55D3);
        put(css, "mediumpurple", 0x9370DB);
        put(css, "mediumseagreen", 0x3CB371);
        put(css, "mediumslateblue", 0x7B68EE);
        put(css, "mediumspringgreen", 0x00FA9A);
        put(css, "mediumturquoise", 0x48D1CC);
        put(css, "mediumvioletred", 0xC71585);
        put(css, "midnightblue", 0x191970);
        put(css, "mintcream", 0xF5FFFA);
        put(css, "mistyrose", 0xFFE4E1);
        put(css, "moccasin", 0xFFE4B5);
        put(css, "navajowhite", 0xFFDEAD);
        put(css, "navy", 0x000080);
        put(css, "oldlace", 0xFDF5E6);
        put(css, "olive", 0x808000);
        put(css, "olivedrab", 0x6B8E23);
        put(css, "orange", 0xFFA500);
        put(css, "orangered", 0xFF4500);
        put(css, "orchid", 0xDA70D6);
        put(css, "palegoldenrod", 0xEEE8AA);
        put(css, "palegreen", 0x98FB98);
        put(css, "paleturquoise", 0xAFEEEE);
        put(css, "palevioletred", 0xDB7093);
        put(css, "papayawhip", 0xFFEFD5);
        put(css, "peachpuff", 0xFFDAB9);
        put(css, "peru", 0xCD853F);
        put(css, "pink", 0xFFC0CB);
        put(css, "plum", 0xDDA0DD);
        put(css, "powderblue", 0xB0E0E6);
        put(css, "purple", 0x800080);
        put(css, "rebeccapurple", 0x663399);
        put(css, "red", 0xFF0000);
        put(css, "rosybrown", 0xBC8F8F);
        put(css, "royalblue", 0x4169E1);
        put(css, "saddlebrown", 0x8B4513);
        put(css, "salmon", 0xFA8072);
        put(css, "sandybrown", 0xF4A460);
        put(css, "seagreen", 0x2E8B57);
        put(css, "seashell", 0xFFF5EE);
        put(css, "sienna", 0xA0522D);
        put(css, "silver", 0xC0C0C0);
        put(css, "skyblue", 0x87CEEB);
        put(css, "slateblue", 0x6A5ACD);
        put(css, "slategray", 0x708090);
        put(css, "slategrey", 0x708090);
        put(css, "snow", 0xFFFAFA);
        put(css, "springgreen", 0x00FF7F);
        put(css, "steelblue", 0x4682B4);
        put(css, "tan", 0xD2B48C);
        put(css, "teal", 0x008080);
        put(css, "thistle", 0xD8BFD8);
        put(css, "tomato", 0xFF6347);
        put(css, "turquoise", 0x40E0D0);
        put(css, "violet", 0xEE82EE);
        put(css, "wheat", 0xF5DEB3);
        put(css, "white", 0xFFFFFF);
        put(css, "whitesmoke", 0xF5F5F5);
        put(css, "yellow", 0xFFFF00);
        put(css, "yellowgreen", 0x9ACD32);
        CSS = Collections.unmodifiableMap(css);
    }
}
