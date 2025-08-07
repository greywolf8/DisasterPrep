package com.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.Route;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SMAPApiClient {
    /**
     * This method is intended for authenticated downloads from the SMAP SPL3SMP_E product endpoint.
     * It does not represent a public REST API and requires Earthdata login credentials.
     */
    public void downloadSoilMoistureFile(String localFilePath, String username, String password) throws IOException {
        CmrClient cmrClient = new CmrClient();
        String fileUrl = cmrClient.getLatestSmapFileUrl();

        if (fileUrl == null) {
            throw new IOException("No downloadable SMAP file found.");
        }

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new JavaNetCookieJar(cookieManager))
            .authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    if (response.request().header("Authorization") != null) {
                        return null; // Give up, we've already attempted to authenticate.
                    }
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
                }
            })
            .build();

        Request request = new Request.Builder()
            .url(fileUrl)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP request failed with code: " + response.code() + " " + response.message() + "\n" + response.body().string());
            }

            try (FileOutputStream out = new FileOutputStream(localFilePath)) {
                out.write(response.body().bytes());
            }
        }
    }

    public String getSoilMoistureDataFromFile(String h5FilePath, double targetLat, double targetLon) {
        try (io.jhdf.HdfFile hdfFile = new io.jhdf.HdfFile(java.nio.file.Paths.get(h5FilePath))) {
            System.out.println("Available datasets:");
            printGroup(hdfFile, "");

            io.jhdf.api.Dataset latDataset = hdfFile.getDatasetByPath("/Soil_Moisture_Retrieval_Data_AM/latitude");
            io.jhdf.api.Dataset lonDataset = hdfFile.getDatasetByPath("/Soil_Moisture_Retrieval_Data_AM/longitude");
            io.jhdf.api.Dataset soilDataset = hdfFile.getDatasetByPath("/Soil_Moisture_Retrieval_Data_AM/soil_moisture");

            float[][] latitudes = (float[][]) latDataset.getData();
            float[][] longitudes = (float[][]) lonDataset.getData();
            float[][] soilMoisture = (float[][]) soilDataset.getData();

            int closestX = 0;
            int closestY = 0;
            double minDistance = Double.MAX_VALUE;

            // Find the closest point with valid data (not -9999)
            boolean foundValidPoint = false;
            for (int i = 0; i < latitudes.length; i++) {
                for (int j = 0; j < latitudes[0].length; j++) {
                    // Skip invalid data points
                    if (soilMoisture[i][j] < -9000) continue;
                    
                    double dist = Math.pow(latitudes[i][j] - targetLat, 2) + 
                                 Math.pow(longitudes[i][j] - targetLon, 2);
                    if (dist < minDistance) {
                        minDistance = dist;
                        closestX = i;
                        closestY = j;
                        foundValidPoint = true;
                    }
                }
            }

            if (!foundValidPoint) {
                return "NO_DATA";
            }

            // Return the soil moisture value with 4 decimal places
            return String.format("%.4f", soilMoisture[closestX][closestY]);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading HDF5 data: " + e.getMessage();
        }
    }

    public String getLatestSmapData(double lat, double lon, String username, String password) throws IOException {
        String tempH5File = "temp_smap.h5";
        downloadSoilMoistureFile(tempH5File, username, password);
        return getSoilMoistureDataFromFile(tempH5File, lat, lon);
    }

    private void printGroup(io.jhdf.api.Group group, String indent) {
        for (io.jhdf.api.Node node : group) {
            System.out.println(indent + node.getName() + " (" + node.getType() + ")");
            if (node instanceof io.jhdf.api.Group) {
                printGroup((io.jhdf.api.Group) node, indent + "  ");
            }
        }
    }
}