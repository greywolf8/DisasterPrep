package com.example;

import okhttp3.*;
import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;

public class AddressToCoordsClient {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GEOAPIFY_API_KEY");
    private static final String BASE = "https://api.geoapify.com/v1/geocode/search";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public JsonObject geocode(String address) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE).newBuilder()
            .addQueryParameter("text", address)
            .addQueryParameter("format", "json")
            .addQueryParameter("apiKey", API_KEY)
            .build();

        Request req = new Request.Builder().url(url).get().build();
        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("Geoapify failed: " + res);
            JsonObject root = gson.fromJson(res.body().string(), JsonObject.class);
            JsonArray features = root.getAsJsonArray("features");
            if (features == null || features.size() == 0) {
                // Fallback for 'results' key, though 'features' is expected for geocode search
                features = root.getAsJsonArray("results");
                if (features == null || features.size() == 0) {
                    throw new IOException("No match found for the address. Neither 'features' nor 'results' array found in the API response.");
                }
            }

            JsonObject feature = features.get(0).getAsJsonObject();

            // The lat, lon, and formatted address are directly in the feature object
            if (!feature.has("lat") || !feature.has("lon") || !feature.has("formatted")) {
                throw new IOException("The 'lat', 'lon', or 'formatted' field is missing in the API response feature. Full feature: " + feature);
            }

            JsonObject out = new JsonObject();
            out.addProperty("lat", feature.get("lat").getAsDouble());
            out.addProperty("lon", feature.get("lon").getAsDouble());
            out.addProperty("formatted_address", feature.get("formatted").getAsString());
            return out;
        }
    }
}