package com.example;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;

public class CmrClient {

    private static final String CMR_URL = "https://cmr.earthdata.nasa.gov/search/granules.json";
    private static final String COLLECTION_ID = "C2938664763-NSIDC_CPRD";

    public String getLatestSmapFileUrl() throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(CMR_URL).newBuilder();
        urlBuilder.addQueryParameter("collection_concept_id", COLLECTION_ID);
        urlBuilder.addQueryParameter("sort_key", "-start_date");
        urlBuilder.addQueryParameter("page_size", "1");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray entries = jsonObject.getAsJsonObject("feed").getAsJsonArray("entry");

            if (entries.size() > 0) {
                JsonObject latestEntry = entries.get(0).getAsJsonObject();
                JsonArray links = latestEntry.getAsJsonArray("links");
                for (int i = 0; i < links.size(); i++) {
                    JsonObject link = links.get(i).getAsJsonObject();
                    if (link.has("rel") && link.get("rel").getAsString().endsWith("data#")) {
                        return link.get("href").getAsString();
                    }
                }
            }
            return null; // Return null if no file is found
        }
    }
}