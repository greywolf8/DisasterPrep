package com.example;

import okhttp3.*;
import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;

public class NasaEonetClient {
    private static final String API_KEY = Dotenv.load().get("NASA_API_KEY");
    private static final OkHttpClient client = new OkHttpClient();
    private static final double DELTA = 1.0; // ± degrees longitude/latitude

    /**
     * Fetch events open in last N days, filtered within bounding box around lat/lon.
     */
    public JsonObject getNearbyEvents(double lat, double lon) throws IOException {
        double minLon = lon - DELTA;
        double maxLon = lon + DELTA;
        double minLat = lat - DELTA;
        double maxLat = lat + DELTA;

        String url = String.format(
            "https://eonet.gsfc.nasa.gov/api/v3/events?status=open&limit=50&bbox=%f,%f,%f,%f&api_key=%s",
            minLon, maxLat, maxLon, minLat, API_KEY);

        Request req = new Request.Builder().url(url).get().build();
        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("EONET API fail: " + res);
            return JsonParser.parseString(res.body().string()).getAsJsonObject();
        }
    }
}