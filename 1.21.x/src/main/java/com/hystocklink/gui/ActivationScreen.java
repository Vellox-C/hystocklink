package com.hystocklink.gui;

import com.hystocklink.SessionOverride;
import com.hystocklink.api.ActivationClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

public class ActivationScreen extends Screen {

    private TextFieldWidget codeField;
    private String statusMessage = "";
    private boolean loading = false;
    private boolean success = false;

    public ActivationScreen() {
        super(Text.literal("Activate Account"));
    }

    @Override
    protected void init() {
        codeField = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, Text.literal("Redemption code"));
        codeField.setMaxLength(64);
        addDrawableChild(codeField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Activate"), button -> {
            if (!loading && !success) activate();
        }).dimensions(width / 2 - 102, height / 2 + 15, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
            client.setScreen(null);
        }).dimensions(width / 2 + 4, height / 2 + 15, 98, 20).build());
    }

    private void activate() {
        String code = codeField.getText().trim();
        if (code.isEmpty()) return;
        loading = true;
        statusMessage = "Activating...";

        Thread.ofVirtual().start(() -> {
            try {
                ActivationClient.ActivationResult result = ActivationClient.activate(code);
                UUID uuid = UUID.fromString(addDashes(result.uuid()));
                Session session = new Session(
                        result.username(), uuid, result.token(),
                        Optional.empty(), Optional.empty(), Session.AccountType.MSA
                );
                SessionOverride.set(session);
                success = true;
                statusMessage = "§aLogged in as " + result.username() + ". You can now join a server.";
                Thread.sleep(2000);
                MinecraftClient.getInstance().execute(() -> client.setScreen(null));
            } catch (Exception e) {
                statusMessage = "§c" + e.getMessage();
            } finally {
                loading = false;
            }
        });
    }

    private String addDashes(String uuid) {
        if (uuid.contains("-")) return uuid;
        return uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 40, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Enter your redemption code from hystock.shop"),
                width / 2, height / 2 - 25, 0xAAAAAA);
        if (!statusMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(statusMessage), width / 2, height / 2 + 42, 0xFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }
}
