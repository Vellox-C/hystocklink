package com.hystocklink;

import com.hystocklink.api.ActivationClient;
import com.hystocklink.mixin.MinecraftAccessor;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.telemetry.ClientTelemetryManager;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SessionOverride {

    private static User originalUser = null;

    // Must be called from a background thread — makes network calls (profile fetch, properties fetch).
    public static void inject(ActivationClient.ActivationResult result) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftAccessor accessor = (MinecraftAccessor) mc;

        if (originalUser == null) {
            originalUser = mc.getUser();
        }

        UUID uuid = UUID.fromString(addDashes(result.uuid()));
        Optional<String> xuid = result.xuid().isEmpty() ? Optional.empty() : Optional.of(result.xuid());
        // 5-arg constructor — MC 1.21.9 does not have User.Type.
        User user = new User(result.username(), uuid, result.token(), xuid, Optional.empty());

        // Create a fresh auth service. In 1.21.9 the old authenticationService field was removed.
        YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(mc.getProxy());
        UserApiService apiService = authService.createUserApiService(result.token());

        // Profile fetch method was removed in 1.21.x; pass null (MC fetches lazily when needed).
        ProfileResult profile = null;

        UserApiService.UserProperties properties;
        try {
            properties = apiService.fetchProperties();
        } catch (Throwable ignored) {
            properties = UserApiService.OFFLINE_PROPERTIES;
        }

        CompletableFuture<ProfileResult> profileFuture = CompletableFuture.completedFuture(profile);
        CompletableFuture<UserApiService.UserProperties> propertiesFuture = CompletableFuture.completedFuture(properties);
        PlayerSocialManager social = new PlayerSocialManager(mc, apiService);
        ClientTelemetryManager telemetry = new ClientTelemetryManager(mc, apiService, user);
        ProfileKeyPairManager keyPair = ProfileKeyPairManager.create(apiService, user, mc.gameDirectory.toPath());
        ReportingContext reporting = ReportingContext.create(ReportEnvironment.local(), apiService);

        // Flush all session fields on the main thread.
        mc.execute(() -> {
            accessor.ias$user(user);
            accessor.ias$profileFuture(profileFuture);
            accessor.ias$userApiService(apiService);
            accessor.ias$userPropertiesFuture(propertiesFuture);
            accessor.ias$playerSocialManager(social);
            accessor.ias$telemetryManager(telemetry);
            accessor.ias$profileKeyPairManager(keyPair);
            accessor.ias$reportingContext(reporting);
        });
    }

    public static void clear() {
        if (originalUser != null) {
            Minecraft mc = Minecraft.getInstance();
            MinecraftAccessor accessor = (MinecraftAccessor) mc;
            mc.execute(() -> accessor.ias$user(originalUser));
            originalUser = null;
        }
    }

    private static String addDashes(String uuid) {
        if (uuid.contains("-")) return uuid;
        return uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    }
}
