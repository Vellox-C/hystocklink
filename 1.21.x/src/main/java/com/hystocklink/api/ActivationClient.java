package com.hystocklink.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ActivationClient {

    private static final String ACTIVATE_URL = "https://hystock.shop/api/activate";

    public record ActivationResult(String token, String uuid, String username) {}

    public static ActivationResult activate(String code) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String body = "{\"code\":\"" + code + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ACTIVATE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 404 -> throw new Exception("Invalid or expired code.");
            case 410 -> throw new Exception("Code already used.");
            case 200 -> {}
            default  -> throw new Exception("Server error (" + response.statusCode() + ").");
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return new ActivationResult(
                json.get("token").getAsString(),
                json.get("uuid").getAsString(),
                json.get("username").getAsString()
        );
    }
}
