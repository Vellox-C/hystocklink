package com.hystocklink;

import com.mojang.blaze3d.platform.InputConstants;
import com.hystocklink.gui.AccountWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.components.events.GuiEventListener;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HystockLinkClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("hystocklink");
    public static KeyMapping KEY_MOVE;
    public static AccountWidget activeWidget;

    // Fires after DEFAULT_PHASE — re-inserts our widget as the last input listener
    // so MC's reversed-iteration dispatch gives it highest click priority.
    private static final Identifier PHASE_LAST =
            Identifier.fromNamespaceAndPath("hystocklink", "last");

    public static boolean isMoveKeyHeld() {
        if (KEY_MOVE == null) return false;
        long window = Minecraft.getInstance().getWindow().handle();
        InputConstants.Key key = KEY_MOVE.getDefaultKey();
        return key.getType() == InputConstants.Type.KEYSYM
                && GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
    }

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("hystocklink", "hystocklink")
        );
        KEY_MOVE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hystocklink.move_button",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                category
        ));
        HystockConfig.init(FabricLoader.getInstance().getGameDir());

        // Re-insert our widget as the last input listener after all other mods' AFTER_INIT
        // handlers have run. This gives our widget the highest input priority in MC's standard
        // children dispatch (reversed iteration: last child = first to receive clicks).
        ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, PHASE_LAST);
        ScreenEvents.AFTER_INIT.register(PHASE_LAST, (mc, screen, width, height) -> {
            if (!(screen instanceof TitleScreen) || activeWidget == null) return;
            @SuppressWarnings("unchecked")
            List<GuiEventListener> listeners = (List<GuiEventListener>) (List<?>) screen.children();
            listeners.remove(activeWidget);
            listeners.add(activeWidget);
        });

        ScreenEvents.AFTER_INIT.register((mc, screen, width, height) -> {
            if (!(screen instanceof TitleScreen)) return;

            // Render on top — afterExtract fires after Screen extraction and all other mods' injections.
            ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, delta) -> {
                if (activeWidget != null) {
                    activeWidget.extractRenderState(graphics, mouseX, mouseY, delta);
                }
            });

            ScreenMouseEvents.allowMouseDrag(screen).register((s, event, dx, dy) -> {
                if (event.button() != 0) return true;
                AccountWidget w = activeWidget;
                if (w != null && w.isDragging()) {
                    w.performDrag((int) event.x(), (int) event.y());
                    return false;
                }
                return true;
            });

            ScreenMouseEvents.allowMouseRelease(screen).register((s, event) -> {
                if (event.button() != 0) return true;
                AccountWidget w = activeWidget;
                if (w != null && w.isDragging()) {
                    w.endDrag();
                    return false;
                }
                return true;
            });
        });

        LOGGER.info("HystockLink initialized.");
    }
}
