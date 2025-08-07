package com.example;

import okhttp3.*;
import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;

public class OpenRouterClient {
    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = Dotenv.load().get("OPENROUTER_API_KEY");
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Sends combined data to OpenRouter and returns AI response content.
     */
    public String getDisasterPlan(JsonObject combinedPayload) throws IOException {
        // Build the user message with prompt and payload
        String userContent = "You are an expert disaster preparedness analyst. " +
            "Based on the JSON data below, provide:\n" +
            "1. Overall threat level (Low, Moderate, High, Critical) with justification.\n" +
            "2. Summary of current hazards and distances.\n" +
            "3. Recommended immediate actions and evacuation guidance.\n" +
            "4. Preventive tips, emergency kit checklist, and local resources.\n" +
            "5. Forecast implications next 48 hours.\n\n" +
            "JSON Data:\n" +
            gson.toJson(combinedPayload);

        JsonObject body = new JsonObject();
        body.addProperty("model", "mistralai/mistral-7b-instruct");
        JsonArray messages = new JsonArray();
        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", "You are a helpful and knowledgeable AI disaster assistant.");
        messages.add(sys);
        JsonObject usr = new JsonObject();
        usr.addProperty("role", "user");
        usr.addProperty("content", userContent);
        messages.add(usr);
        body.add("messages", messages);
        body.addProperty("max_tokens", 700);
        body.addProperty("temperature", 0.2);

        Request request = new Request.Builder()
                .url(OPENROUTER_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("OpenRouter API call failed: " + response);
            JsonObject respJson = gson.fromJson(response.body().string(), JsonObject.class);
            JsonObject choice = respJson.getAsJsonArray("choices").get(0).getAsJsonObject();
            return choice.getAsJsonObject("message").get("content").getAsString();
        }
    }
}