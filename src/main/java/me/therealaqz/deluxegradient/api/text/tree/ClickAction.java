// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.text.tree;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Supported client actions. No action executes on the server when a message is sent. */
public enum ClickAction {
    OPEN_URL, SUGGEST_COMMAND, RUN_COMMAND, COPY_TO_CLIPBOARD;

    public static ClickAction byName(final String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException unknown) {
            return null;
        }
    }

    /** Validate after expanding attributes, when a URL/command is actually known. */
    public boolean accepts(final String value) {
        if (value.isEmpty() || value.length() > 2048) return false;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) return false;
        }
        if (this == OPEN_URL) {
            try {
                final URI uri = new URI(value);
                return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                        && uri.getHost() != null && uri.getUserInfo() == null;
            } catch (final URISyntaxException invalid) {
                return false;
            }
        }
        return this == COPY_TO_CLIPBOARD || value.startsWith("/");
    }
}
