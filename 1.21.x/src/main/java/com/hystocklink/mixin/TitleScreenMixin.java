package com.hystocklink.mixin;

import com.hystocklink.gui.ActivationScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addActivateButton(CallbackInfo ci) {
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Activate Account"),
                button -> MinecraftClient.getInstance().setScreen(new ActivationScreen())
        ).dimensions(5, this.height - 25, 120, 20).build());
    }
}
