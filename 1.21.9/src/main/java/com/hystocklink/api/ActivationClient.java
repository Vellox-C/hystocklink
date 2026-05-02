package com.hystocklink.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Base64;

public class ActivationClient {

    public record ActivationResult(String token, String uuid, String username, String xuid) {}

    // The token is a Minecraft JWT (ygg.token from auth_runner).
    // Decode the payload section to extract UUID and username.
    // No server call needed — MC servers validate the token themselves.
    public static ActivationResult activate(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new Exception("Not a valid token. Paste the full access token.");

        // JWT uses Base64URL encoding (no padding). Pad to a multiple of 4.
        String padded = parts[1] + "=".repeat((4 - parts[1].length() % 4) % 4);
        String json = new String(Base64.getUrlDecoder().decode(padded));

        JsonObject payload = JsonParser.parseString(json).getAsJsonObject();

        // UUID lives at profiles.mc
        JsonObject profiles = payload.getAsJsonObject("profiles");
        if (profiles == null || !profiles.has("mc"))
            throw new Exception("Token has no Minecraft profile.");
        String uuid = profiles.get("mc").getAsString();

        // Username lives at pfd[0].name
        JsonArray pfd = payload.getAsJsonArray("pfd");
        if (pfd == null || pfd.isEmpty())
            throw new Exception("Token has no profile data.");
        String username = pfd.get(0).getAsJsonObject().get("name").getAsString();

        // xuid is at the top level — required for User to behave correctly during save/telemetry
        String xuid = payload.has("xuid") ? payload.get("xuid").getAsString() : "";

        return new ActivationResult(token, uuid, username, xuid);
    }
}
