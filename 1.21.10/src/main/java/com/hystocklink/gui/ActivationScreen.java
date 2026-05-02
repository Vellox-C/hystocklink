package com.hystocklink.gui;

import com.hystocklink.SessionOverride;
import com.hystocklink.api.ActivationClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ActivationScreen extends Screen {

    private EditBox codeField;
    private String statusMessage = "";
    private boolean loading = false;
    private boolean success = false;

    public ActivationScreen() {
        super(Component.literal("Activate Account"));
    }

    @Override
    protected void init() {
        codeField = new EditBox(this.font, width / 2 - 100, height / 2 - 10, 200, 20, Component.literal("Access token"));
        codeField.setMaxLength(2048);
        addRenderableWidget(codeField);

        addRenderableWidget(Button.builder(Component.literal("Activate"), button -> {
            if (!loading && !success) activate();
        }).bounds(width / 2 - 102, height / 2 + 15, 98, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            this.minecraft.setScreen(null);
        }).bounds(width / 2 + 4, height / 2 + 15, 98, 20).build());
    }

    private void activate() {
        String code = codeField.getValue().trim();
        if (code.isEmpty()) return;
        loading = true;
        statusMessage = "Activating...";

        Thread.ofVirtual().start(() -> {
            try {
                ActivationClient.ActivationResult result = ActivationClient.activate(code);
                SessionOverride.inject(result);
                success = true;
                statusMessage = "§aLogged in as " + result.username() + ". You can now join a server.";
                Thread.sleep(2000);
                Minecraft.getInstance().execute(() -> this.minecraft.setScreen(null));
            } catch (Exception e) {
                statusMessage = "§c" + e.getMessage();
            } finally {
                loading = false;
            }
        });
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // No-op: suppress blur entirely. Title screen already applied it;
        // calling it again crashes with "Can only blur once per frame".
        guiGraphics.fill(0, 0, width, height, 0xB0000000);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.drawCenteredString(this.font, this.title, width / 2, height / 2 - 40, 0xFFFFFF);
        context.drawCenteredString(this.font,
                Component.literal("Paste your Minecraft access token (ygg.token)"),
                width / 2, height / 2 - 25, 0xAAAAAA);
        if (!statusMessage.isEmpty()) {
            context.drawCenteredString(this.font, Component.literal(statusMessage), width / 2, height / 2 + 42, 0xFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }
}
