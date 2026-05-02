package com.hystocklink;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HystockConfig {
    private static final Gson GSON = new Gson();
    private static Path configPath;
    private static int savedX = -1;
    private static int savedY = -1;

    public static void init(Path gameDir) {
        configPath = gameDir.resolve("config/hystocklink.json");
        load();
    }

    public static int getSavedX(int screenWidth) {
        return savedX < 0 ? 20 : savedX;
    }

    public static int getSavedY(int screenHeight) {
        return savedY < 0 ? screenHeight - 73 : savedY;
    }

    public static void save(int x, int y) {
        savedX = x;
        savedY = y;
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject obj = new JsonObject();
            obj.addProperty("x", x);
            obj.addProperty("y", y);
            Files.writeString(configPath, GSON.toJson(obj));
        } catch (IOException e) {
            HystockLinkClient.LOGGER.warn("HystockLink: failed to save config: {}", e.getMessage());
        }
    }

    private static void load() {
        if (configPath == null || !Files.exists(configPath)) return;
        try {
            JsonObject obj = GSON.fromJson(Files.readString(configPath), JsonObject.class);
            if (obj.has("x")) savedX = obj.get("x").getAsInt();
            if (obj.has("y")) savedY = obj.get("y").getAsInt();
        } catch (Exception e) {
            HystockLinkClient.LOGGER.warn("HystockLink: failed to load config: {}", e.getMessage());
        }
    }
}
