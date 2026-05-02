package com.hystocklink.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("user") @Mutable
    void ias$user(User user);

    @Accessor("profileFuture") @Mutable
    void ias$profileFuture(CompletableFuture<ProfileResult> future);

    @Accessor("userApiService") @Mutable
    void ias$userApiService(UserApiService service);

    @Accessor("userPropertiesFuture") @Mutable
    void ias$userPropertiesFuture(CompletableFuture<UserApiService.UserProperties> future);

    @Accessor("playerSocialManager") @Mutable
    void ias$playerSocialManager(PlayerSocialManager manager);

    @Accessor("telemetryManager") @Mutable
    void ias$telemetryManager(ClientTelemetryManager manager);

    @Accessor("profileKeyPairManager") @Mutable
    void ias$profileKeyPairManager(ProfileKeyPairManager manager);

    @Accessor("reportingContext") @Mutable
    void ias$reportingContext(ReportingContext context);
}
