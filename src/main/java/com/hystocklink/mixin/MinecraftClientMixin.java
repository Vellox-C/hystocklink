package com.hystocklink.mixin;

import com.hystocklink.SessionOverride;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    // Returns the override session when set, so all server auth uses the activated account.
    @Inject(method = "getSession", at = @At("RETURN"), cancellable = true)
    private void overrideSession(CallbackInfoReturnable<Session> cir) {
        Session override = SessionOverride.get();
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
