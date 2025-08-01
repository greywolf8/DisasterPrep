package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@RestController
public class Main {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Dotenv dotenv = Dotenv.load();
    private static final String OPENROUTER_API_KEY = dotenv.get("OPENROUTER_API_KEY");
    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/evaluate")
    public String evaluate(@RequestBody String address) {
        AddressToCoordsClient coordsClient = new AddressToCoordsClient();
        double lat = 0;
        double lon = 0;
        JsonObject coords;

        try {
            coords = coordsClient.geocode(address);
            lat = coords.get("lat").getAsDouble();
            lon = coords.get("lon").getAsDouble();
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error geocoding address: " + e.getMessage());
            return gson.toJson(errorResponse);
        }

        WeatherApiClient weatherClient = new WeatherApiClient();
        CombinedGeoClient geoClient = new CombinedGeoClient();
        NasaEonetClient nasaClient = new NasaEonetClient();
        JsonObject combinedData = new JsonObject();

        try {
            combinedData.add("weather", weatherClient.getWeather(lat, lon));
            combinedData.add("geoInfo", geoClient.getGeoInfo(lat, lon));
            combinedData.add("eonetEvents", nasaClient.getNearbyEvents(lat, lon));

            OpenRouterClient openRouterClient = new OpenRouterClient();
            String disasterPlan = openRouterClient.getDisasterPlan(combinedData);

            try (PrintWriter out = new PrintWriter(new FileWriter("output.txt"))) {
                out.println("--- Combined JSON Data ---");
                out.println(gson.toJson(combinedData));
                out.println("\n--- Disaster Preparedness Plan ---");
                out.println(disasterPlan);
            }

            JsonObject response = new JsonObject();
            response.add("rawData", combinedData);
            response.addProperty("disasterPlan", disasterPlan);

            return gson.toJson(response);
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error processing disaster plan: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        String context;
        try {
            context = new String(Files.readAllBytes(Paths.get("output.txt")));
            if (context.trim().isEmpty() || !context.contains("--- Disaster Preparedness Plan ---")) {
                return "The output.txt file is empty or does not contain a disaster plan. " +
                       "Please run the /evaluate endpoint first to generate the necessary data.";
            }
        } catch (IOException e) {
            return "Could not read output.txt. Please run the /evaluate endpoint first.";
        }

        try {
            return getChatResponse(context, message);
        } catch (IOException e) {
            return "Error getting response from AI: " + e.getMessage();
        }
    }

    private String getChatResponse(String context, String question) throws IOException {
        String systemPrompt = "You are a helpful disaster preparedness assistant. " +
            "Use the following information to answer the user's questions about their local situation and disaster plan. " +
            "Be concise and focus on the provided data. Here is the data:\n\n" + context;

        JsonObject messageUser = new JsonObject();
        messageUser.addProperty("role", "user");
        messageUser.addProperty("content", question);

        JsonObject messageSystem = new JsonObject();
        messageSystem.addProperty("role", "system");
        messageSystem.addProperty("content", systemPrompt);

        JsonArray messages = new JsonArray();
        messages.add(messageSystem);
        messages.add(messageUser);

        JsonObject payload = new JsonObject();
        payload.add("messages", messages);
        payload.addProperty("model", "openai/gpt-3.5-turbo");

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
            gson.toJson(payload),
            MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonObject responseJson = gson.fromJson(response.body().string(), JsonObject.class);
            return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject()
                               .getAsJsonObject("message").get("content").getAsString();
        }
    }
}