package com.example;

import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;

public class CombinedGeoClient {
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Open‑Elevation endpoint (rate‑limited ~1k/mo free)
    private static final String ELEVATION_BASE = "https://api.open-elevation.com/api/v1/lookup";

    // Nominatim public reverse geocode
    private static final String NOMINATIM_BASE = "https://nominatim.openstreetmap.org/reverse";

    /**
     * Fetch elevation in meters from Open‑Elevation.
     */
    public double getElevation(double lat, double lon) throws IOException {
        String url = String.format("%s?locations=%f,%f", ELEVATION_BASE, lat, lon);
        Request req = new Request.Builder().url(url).get().build();
        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("Elev fail: " + res);
            JsonObject root = gson.fromJson(res.body().string(), JsonObject.class);
            JsonArray results = root.getAsJsonArray("results");
            if (results == null || results.size() == 0) throw new IOException("No elevation result");
            return results.get(0).getAsJsonObject().get("elevation").getAsDouble();
        }
    }

    /**
     * Reverse-geocode using Nominatim (OpenStreetMap).
     * Returns display_name and address components.
     */
    public JsonObject reverseGeocode(double lat, double lon) throws IOException {
        HttpUrl url = HttpUrl.parse(NOMINATIM_BASE).newBuilder()
            .addQueryParameter("format", "json")
            .addQueryParameter("lat", String.valueOf(lat))
            .addQueryParameter("lon", String.valueOf(lon))
            .addQueryParameter("addressdetails", "1")
            .build();

        Request req = new Request.Builder()
            .url(url)
            .header("User-Agent", "MyDisasterApp/1.0 (youremail@example.com)")
            .get().build();

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("Geo fail: " + res);
            return gson.fromJson(res.body().string(), JsonObject.class);
        }
    }

    /**
     * Combined method returning a structured JSON object:
     * {
     *   "elevationMeters": 123.4,
     *   "location": {
     *     "display_name": "...",
     *     "address": { ... }
     *   }
     * }
     */
    public JsonObject getGeoInfo(double lat, double lon) throws IOException {
        double elev = getElevation(lat, lon);
        JsonObject geo = reverseGeocode(lat, lon);

        JsonObject out = new JsonObject();
        out.addProperty("elevationMeters", elev);

        JsonObject loc = new JsonObject();
        loc.add("address", geo.getAsJsonObject("address"));
        loc.addProperty("display_name", geo.get("display_name").getAsString());
        out.add("location", loc);

        return out;
    }

}