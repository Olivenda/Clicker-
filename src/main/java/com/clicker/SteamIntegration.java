package com.clicker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Thin Steamworks Web API integration using the official HTTP endpoints.
 * This class intentionally avoids any spoofing and expects valid authentication tokens.
 */
public class SteamIntegration {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiKey;
    private final String appId;

    public SteamIntegration(String apiKey, String appId) {
        this.apiKey = apiKey;
        this.appId = appId;
    }

    public void synchronizeInventory(Inventory inventory, String steamId, String authSessionTicket) throws Exception {
        // Inventory Service endpoint: https://partner.steamgames.com/doc/features/inventory/inventory_service
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.steampowered.com/IEconService/GetInventory/v1/?key=" + apiKey + "&steamid=" + steamId + "&appid=" + appId))
                .header("X-Authentication-Token", authSessionTicket)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to sync inventory: " + response.statusCode());
        }
        // Parsing of live inventory is intentionally omitted; callers should merge server-trusted data.
    }

    public void submitTradeOffer(String partnerSteamId, List<Skin> offered, List<Skin> requested) throws Exception {
        // Reference: https://partner.steamgames.com/doc/webapi/IEconService#SendItemTradingOffer
        String body = "partner=" + partnerSteamId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.steampowered.com/IEconService/SendItemTradingOffer/v1/"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void validateLogin(String authTicket) throws Exception {
        // Reference: https://partner.steamgames.com/doc/features/auth#client_to_backend_auth
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.steampowered.com/ISteamUserAuth/AuthenticateUserTicket/v1/"))
                .POST(HttpRequest.BodyPublishers.ofString("ticket=" + authTicket + "&appid=" + appId))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Steam authentication failed: " + response.statusCode());
        }
    }
}
