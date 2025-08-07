package com.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import ucar.ma2.InvalidRangeException;
import java.util.Optional;

@SpringBootApplication(scanBasePackages = {"com.example"})
@RestController
public class Main implements WebMvcConfigurer {
    
    private final IMERGApiClient imergClient = new IMERGApiClient();
    private final PrecipitationReader precipitationReader = new PrecipitationReader();
    private static final String PRECIPITATION_FILE = "precipitation_data.nc4";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Dotenv dotenv = Dotenv.load();
    private static final String OPENROUTER_API_KEY = dotenv.get("OPENROUTER_API_KEY");
    private static final OkHttpClient client = new OkHttpClient();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/", "file:./")
                .setCachePeriod(0);
    }

    public static void main(String[] args) {
        // Try port 8080, if busy try 8081
        try {
            SpringApplication.run(Main.class, args);
        } catch (Exception e) {
            System.setProperty("server.port", "8081");
            SpringApplication.run(Main.class, args);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/precipitation")
    public String getPrecipitation(
            @RequestParam double lat,
            @RequestParam double lon) {
        JsonObject response = new JsonObject();
        
        try {
            // Download the latest precipitation data
            try {
                imergClient.downloadLatestPrecipitationData(PRECIPITATION_FILE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                response.addProperty("status", "error");
                response.addProperty("message", "Download was interrupted: " + e.getMessage());
                return gson.toJson(response);
            } catch (IOException e) {
                response.addProperty("status", "error");
                response.addProperty("message", "Error downloading precipitation data: " + e.getMessage());
                return gson.toJson(response);
            }
            
            try {
                // Read precipitation for the given coordinates
                Optional<Float> precipitation = precipitationReader.getPrecipitation(
                        PRECIPITATION_FILE, lat, lon);
                
                if (precipitation.isPresent()) {
                    response.addProperty("precipitation", precipitation.get());
                    response.addProperty("unit", "mm/h");
                    response.addProperty("status", "success");
                } else {
                    response.addProperty("status", "no_data");
                    response.addProperty("message", "No precipitation data available for the specified location");
                }
            } catch (IOException | InvalidRangeException e) {
                response.addProperty("status", "error");
                response.addProperty("message", "Error reading precipitation data: " + e.getMessage());
                e.printStackTrace();
            }
        
            return gson.toJson(response);
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return gson.toJson(errorResponse);
        }
    }
    
    @CrossOrigin(origins = "*")
    @PostMapping("/geocode")
    public String geocode(@RequestBody String address) {
        AddressToCoordsClient coordsClient = new AddressToCoordsClient();
        try {
            JsonObject coords = coordsClient.geocode(address);
            return gson.toJson(coords);
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error geocoding address: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
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
            SMAPApiClient smapClient = new SMAPApiClient();
            String smapData = smapClient.getLatestSmapData(lat, lon, dotenv.get("EARTHDATA_USERNAME"), dotenv.get("EARTHDATA_PASSWORD"));
            combinedData.addProperty("smapSoilMoisture", smapData);

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
    @GetMapping("/api/smap")
    public String getSoilMoistureData(@RequestParam double lat, @RequestParam double lon) {
        try {
            SMAPApiClient smapClient = new SMAPApiClient();
            String smapData = smapClient.getLatestSmapData(lat, lon, 
                dotenv.get("EARTHDATA_USERNAME"), 
                dotenv.get("EARTHDATA_PASSWORD"));
                
            JsonObject response = new JsonObject();
            response.addProperty("lat", lat);
            response.addProperty("lon", lon);
            
            // Handle the NO_DATA case
            if ("NO_DATA".equals(smapData)) {
                response.addProperty("moisture", -9999);
            } else {
                try {
                    double moisture = Double.parseDouble(smapData);
                    response.addProperty("moisture", moisture);
                } catch (NumberFormatException e) {
                    // If there's an error parsing the number, return it as a string
                    response.addProperty("moisture", smapData);
                }
            }
            
            return gson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error retrieving soil moisture data: " + e.getMessage());
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