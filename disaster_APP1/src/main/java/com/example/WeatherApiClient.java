package com.example;

import okhttp3.*;
import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;

public class WeatherApiClient {
    private static final String API_KEY = Dotenv.load().get("WEATHER_API_KEY");
    private static final OkHttpClient client = new OkHttpClient();

    public JsonObject getWeather(double lat, double lon) throws Exception {
        String url = String.format(
            "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s",
            lat, lon, API_KEY);
        Request req = new Request.Builder().url(url).get().build();
        try (Response res = client.newCall(req).execute()) {
            String json = res.body().string();
            return JsonParser.parseString(json).getAsJsonObject();
        }
    }
}
//String sql = "CREATE TABLE users (" + "id INT PRIMARY KEY AUTO_INCREMENT, " + "username VARCHAR(50), " + "email VARCHAR(100)" + ")";