// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api;

import me.therealaqz.deluxegradient.api.color.Gradient;
import me.therealaqz.deluxegradient.api.text.tree.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Public integration contract for DeluxeGradient.
 *
 * <p>Obtain the service from Bukkit's ServicesManager or
 * {@link DeluxeGradientProvider#getOrNull()}. Depend on the API with
 * compileOnly/provided scope and never shade or relocate its classes.
 *
 * <p>Formatting input is trusted, administrator-authored content. Integrations
 * must validate and authorize player input before using parse or colorize.
 * Methods taking a player UUID must run on the thread that owns that player.
 * This contract has no server runtime dependency.
 */
public interface DeluxeGradientApi {

    /**
     * Parses a format string into a reusable tree.
     *
     * <p>Parse once and keep the result. Parsing per message is the single
     * most common performance mistake made with this kind of API.
     *
     * <p>Parsed permissively, because an API caller is another plugin and
     * therefore server-authored. Never hand player-authored text to this
     * method and render it back: every syntax this plugin gates behind a
     * permission would be granted to whoever typed it.
     */
    @NotNull Node parse(@NotNull String format);

    /**
     * Renders a tree to a section sign string.
     *
     * @param hexSupported false quantises to the sixteen legacy colours, for
     *                     old clients or for length limited surfaces
     */
    @NotNull String render(@NotNull Node tree, boolean hexSupported);

    /**
     * Renders a tree the way this plugin would render it for one viewer.
     *
     * <p>The point of this over {@link #render(Node, boolean)} is that the
     * decision about colour depth is made in one place. Today that decision is
     * simply whether the viewer is a player, since every client on the
     * supported range renders 24 bit colour and only the console does not. It
     * may become version dependent or client dependent later, and callers of
     * this method get that for free where callers passing their own boolean do
     * not.
     *
     * <p>Deliberately keyed on {@link UUID} rather than on Bukkit's
     * {@code Player}: {@code :api} has no server dependency, which is what
     * lets it be unit tested and depended on cheaply.
     *
     * @param viewer who is going to read it
     * @return the rendered string, or the same result as
     *         {@code render(tree, false)} if that UUID is not online. Rendering
     *         for somebody who cannot see it is not an error, and the legacy
     *         form is the one that is safe everywhere.
     */
    @NotNull String renderFor(@NotNull UUID viewer, @NotNull Node tree);

    /**
     * Whether this viewer is rendered 24 bit colour.
     *
     * <p>Exposed so a caller doing its own rendering can ask the same question
     * {@link #renderFor(UUID, Node)} answers internally, rather than
     * reimplementing it and drifting.
     *
     * @return false if that UUID is not online
     */
    boolean hexSupported(@NotNull UUID viewer);

    /**
     * Convenience for the common case: parse and render in one call.
     *
     * <p>Only appropriate for text that is not rendered repeatedly.
     */
    @NotNull String colorize(@NotNull String format);

    /**
     * Applies a gradient across arbitrary text.
     */
    @NotNull String apply(@NotNull Gradient gradient, @NotNull String text);

    /**
     * A named preset, searching the chat palette and then the name palette.
     *
     * <p>Presets live in two files, one per slot. This convenience exists for
     * callers that only have a name, and it can therefore return the chat
     * entry when both files define the same id with different colours. Prefer
     * {@link #gradient(Slot, String)} whenever the slot is known.
     *
     * @return the gradient, or {@code null} if no preset has that name
     */
    @Nullable Gradient gradient(@NotNull String presetName);

    /**
     * A named preset from one slot's palette.
     *
     * @return the gradient, or {@code null} if that slot has no such preset
     */
    @Nullable Gradient gradient(@NotNull Slot slot, @NotNull String presetName);

    /**
     * Every preset name currently loaded, across both slots.
     */
    @NotNull Set<String> presetNames();

    /**
     * Every preset name in one slot's palette.
     */
    @NotNull Set<String> presetNames(@NotNull Slot slot);

    /**
     * A player's current selection.
     *
     * <p>Resolving a selection includes checking the permission that unlocks
     * it, and a permission check needs a live player. An offline UUID
     * therefore reports nothing rather than reporting a stored value the
     * player may no longer be entitled to.
     *
     * @return the gradient, or {@code null} if they have none, if the preset
     *         they chose no longer exists, if they have lost permission for
     *         it, or if they are not online
     */
    @Nullable Gradient selection(@NotNull UUID player, @NotNull Slot slot);

    /**
     * A player's name with their name gradient applied, or their plain name.
     *
     * <p>The same value {@code %deluxegradient_name%} produces.
     *
     * @return the rendered name, or an empty string if that UUID is not
     *         online. Empty rather than the plain username because the plain
     *         username is not known here for a player who has never been
     *         loaded, and a method that sometimes guesses is worse than one
     *         that never does. Check the result before substituting it into a
     *         format.
     */
    @NotNull String name(@NotNull UUID player);
}
