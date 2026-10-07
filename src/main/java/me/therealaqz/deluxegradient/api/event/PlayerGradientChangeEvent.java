// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)

package me.therealaqz.deluxegradient.api.event;

import me.therealaqz.deluxegradient.api.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fired before a player's gradient selection changes.
 *
 * <p>{@code :api} has no Bukkit dependency, which is what keeps the colour engine
 * testable without a server and lets other projects depend on it cheaply. So
 * the shape of the change lives here and the Bukkit event that carries it lives
 * in the installed plugin, wrapping this.
 *
 * <p>Listeners register for {@code me.therealaqz.deluxegradient.event.GradientChangeEvent}
 * as normal and call {@code change()} on it to get one of these:
 *
 * <pre>
 *   &#64;EventHandler
 *   public void onGradientChange(GradientChangeEvent event) {
 *       PlayerGradientChangeEvent change = event.change();
 *       if (!entitled(change.player(), change.selection())) {
 *           change.selection("default");   // downgrade rather than refuse
 *       }
 *   }
 * </pre>
 *
 * <p>Before any change a player makes: the menu, {@code /dg set}, {@code /dg
 * clear}, and every step of the custom gradient builder. It does not fire when
 * stored data is loaded from disk or from SQL, because that is not a change,
 * it is the same selection arriving in memory.
 */
public final class PlayerGradientChangeEvent {

    private final UUID player;
    private final Slot slot;
    private final String previous;
    private String selection;
    private boolean cancelled;

    public PlayerGradientChangeEvent(
            final @NotNull UUID player,
            final @NotNull Slot slot,
            final @Nullable String previous,
            final @Nullable String selection
    ) {
        this.player = player;
        this.slot = slot;
        this.previous = previous;
        this.selection = selection;
    }

    public @NotNull UUID player() {
        return this.player;
    }

    /** Which surface is changing. */
    public @NotNull Slot slot() {
        return this.slot;
    }

    /**
     * What they had before, or {@code null} if they had no gradient of their
     * own.
     *
     * <p>{@code null} covers both a player who never chose one and a player
     * who cleared the one they had. The two differ only in which default
     * applies afterwards, which is a resolution detail rather than a choice.
     * For the gradient somebody actually renders with, ask
     * {@code DeluxeGradientApi.selection}.
     */
    public @Nullable String previous() {
        return this.previous;
    }

    /**
     * What they are changing to, or {@code null} when clearing.
     *
     * <p>Otherwise either a preset name or an inline {@code <gradient:...>}
     * definition, exactly as the player expressed it. It is not resolved
     * against the palettes here, so a preset name is a name and not a colour
     * list.
     */
    public @Nullable String selection() {
        return this.selection;
    }

    /**
     * Substitutes a different selection.
     *
     * <p>The use case this exists for is a rank plugin downgrading a choice
     * rather than rejecting it outright, which is a better experience than
     * cancelling and leaving the player wondering why nothing happened.
     *
     * <p>The replacement is stored exactly as given and resolved on the next
     * render, so a preset name that does not exist, or a definition that does
     * not parse, leaves the player rendering plainly. It is not validated
     * here: validating would mean this class knowing about the palettes, and
     * {@code :api} deliberately knows about nothing.
     *
     * <p>Passing {@code null} clears the slot, and clears it in the strong
     * sense: the player renders plainly rather than falling back to whatever
     * default the config sets for their rank.
     *
     * <p>Substituting is ignored once {@link #setCancelled(boolean)} is set,
     * because there is then no change to substitute into.
     */
    public void selection(final @Nullable String replacement) {
        this.selection = replacement;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Cancels the change.
     *
     * <p>The player is told the change was prevented, by the plugin, using
     * {@code gradient.change-cancelled} from their language file. Cancelling
     * silently would have them report it as a bug, so the message is not
     * optional and is not the listener's job to send.
     */
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }
}
