package com.hystocklink.mixin;

import com.hystocklink.HystockConfig;
import com.hystocklink.HystockLinkClient;
import com.hystocklink.gui.AccountWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screens.Screen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin() {
        super(Component.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addAccountWidget(CallbackInfo ci) {
        int x = HystockConfig.getSavedX(this.width);
        int y = HystockConfig.getSavedY(this.height);
        HystockLinkClient.activeWidget = new AccountWidget(x, y);
        addWidget(HystockLinkClient.activeWidget);
    }
}
