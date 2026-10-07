// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Aqz (therealaqz)
package me.therealaqz.deluxegradient.api;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.therealaqz.deluxegradient.api.color.DeluxeColor;
import me.therealaqz.deluxegradient.api.text.tree.ClickAction;
import me.therealaqz.deluxegradient.api.text.tree.ClickNode;
import me.therealaqz.deluxegradient.api.text.tree.GroupNode;
import me.therealaqz.deluxegradient.api.text.tree.Node;
import me.therealaqz.deluxegradient.api.text.tree.Nodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Behavioural coverage of the exported API's trust and lifecycle boundaries. */
class ApiContractTest {
    @AfterEach
    void clearProvider() {
        DeluxeGradientProvider.set(null);
    }

    @Test
    void invalidHexInputIsRejectedInsteadOfInterpretedAsASignedNumber() {
        for (String value : Arrays.asList("#GGGGGG", "-F0000", "+F0000", "#-ab", "#12", "", "#ff0000\n")) {
            assertNull(DeluxeColor.fromHex(value), value);
        }
        assertNull(DeluxeColor.fromHex(null));
        assertEquals(DeluxeColor.of(0xFF00AA), DeluxeColor.fromHex("#f0a"));
    }

    @Test
    void urlActionsRejectExecutableSchemesCredentialsAndMalformedHosts() {
        for (String value : Arrays.asList("javascript:alert(1)", "file:///tmp/example", "ftp://example.com",
                "https://user:password@example.com", "https:///missing-host", "//example.com")) {
            assertFalse(ClickAction.OPEN_URL.accepts(value), value);
        }
        assertTrue(ClickAction.OPEN_URL.accepts("https://example.com/help"));
        assertTrue(ClickAction.OPEN_URL.accepts("http://example.com/help"));
    }

    @Test
    void allClientActionsRejectControlCharactersAndExcessiveLength() {
        for (ClickAction action : ClickAction.values()) {
            assertFalse(action.accepts(""));
            assertFalse(action.accepts("/help\n/other"));
            assertFalse(action.accepts("/help\u0000"));
            char[] oversized = new char[2049];
            Arrays.fill(oversized, 'a');
            oversized[0] = '/';
            assertFalse(action.accepts(new String(oversized)));
        }
    }

    @Test
    void commandActionsRequireAnExplicitCommandPrefix() {
        assertFalse(ClickAction.RUN_COMMAND.accepts("help"));
        assertFalse(ClickAction.SUGGEST_COMMAND.accepts("help"));
        assertTrue(ClickAction.RUN_COMMAND.accepts("/help"));
        assertTrue(ClickAction.SUGGEST_COMMAND.accepts("/help"));
        assertTrue(ClickAction.COPY_TO_CLIPBOARD.accepts("help"));
    }

    @Test
    void nodeChildrenCannotBeChangedThroughTheCallerListOrReturnedList() {
        List<Node> input = new ArrayList<Node>();
        input.add(Nodes.text("first"));
        GroupNode group = Nodes.group(input);
        ClickNode click = Nodes.click(ClickAction.SUGGEST_COMMAND, "/help", input);
        input.clear();
        assertEquals(1, group.children().size());
        assertEquals(1, click.children().size());
        assertThrows(UnsupportedOperationException.class, () -> group.children().clear());
        assertThrows(UnsupportedOperationException.class, () -> click.children().clear());
    }

    @Test
    void literalAndAtomicNodesPreserveTheirInputWithoutParsing() {
        String input = "<click:run_command:'/help'>[item]</click>";
        assertEquals(input, Nodes.text(input).content());
        assertEquals(input, Nodes.atomicText(input).content());
        assertFalse(Nodes.text(input).isAtomic());
        assertTrue(Nodes.atomicText(input).isAtomic());
    }

    @Test
    void unavailableProviderFailsExplicitlyAndClearsOnDisable() {
        DeluxeGradientProvider.set(null);
        assertFalse(DeluxeGradientProvider.isAvailable());
        assertNull(DeluxeGradientProvider.getOrNull());
        assertThrows(IllegalStateException.class, DeluxeGradientProvider::get);
        DeluxeGradientApi service = (DeluxeGradientApi) Proxy.newProxyInstance(
                DeluxeGradientApi.class.getClassLoader(), new Class<?>[] {DeluxeGradientApi.class},
                (proxy, method, args) -> { throw new UnsupportedOperationException("test service"); });
        DeluxeGradientProvider.set(service);
        assertSame(service, DeluxeGradientProvider.get());
        assertTrue(DeluxeGradientProvider.isAvailable());
        DeluxeGradientProvider.set(null);
        assertNull(DeluxeGradientProvider.getOrNull());
    }

    @Test
    void invalidSlotDoesNotSilentlySelectADifferentPermissionScope() {
        assertEquals(Slot.CHAT, Slot.byId("CHAT"));
        assertEquals(Slot.NAME, Slot.byId("Name"));
        assertNull(Slot.byId("other"));
        assertNull(Slot.byId(""));
        assertNull(Slot.byId(null));
    }
}
