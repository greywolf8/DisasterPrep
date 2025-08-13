package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class DisasterChatbot {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String OPENROUTER_API_KEY = dotenv.get("OPENROUTER_API_KEY");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String context;
        try {
            context = new String(Files.readAllBytes(Paths.get("output.txt")));
            if (context.trim().isEmpty() || !context.contains("--- Disaster Preparedness Plan ---")) {
                System.out.println("The output.txt file is empty or does not contain a disaster plan.");
                System.out.println("Please run the main application first to generate the necessary data.");
                System.out.println("Usage: java com.example.Main \"<address>\"");
                return;
            }
        } catch (IOException e) {
            System.out.println("Could not read output.txt. Please run the main application first.");
            System.out.println("Usage: java com.example.Main \"<address>\"");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Disaster Preparedness Chatbot.");
        System.out.println("You can ask questions about the disaster plan. Type 'exit' to quit.");

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Stay safe! Exiting chatbot.");
                break;
            }

            try {
                String aiResponse = getChatResponse(context, userInput);
                System.out.println("Chatbot: " + aiResponse);
            } catch (IOException e) {
                System.err.println("Error getting response from AI: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static String getChatResponse(String context, String question) throws IOException {
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

        RequestBody body = RequestBody.create(
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