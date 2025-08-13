import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FIRMSApiClient {
    private static final String FIRMS_API_URL = "https://firms.modaps.eosdis.nasa.gov/api/area/csv/%s/VIIRS_NOAA20_NRT/world/1";

    public String fetchFireData(String apiKey) throws Exception {
        String url = String.format(FIRMS_API_URL, apiKey);
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // Returns CSV data
    }

    public static void main(String[] args) throws Exception {
        FIRMSApiClient client = new FIRMSApiClient();
        String data = client.fetchFireData("YOUR_API_KEY"); // Replace with actual key
        System.out.println(data);
    }
}