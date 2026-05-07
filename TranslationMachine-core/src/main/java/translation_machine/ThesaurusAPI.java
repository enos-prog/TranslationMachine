package translation_machine;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*URL url = new URL("https://api.api-ninjas.com/v1/thesaurus?word=elegant");
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestProperty("accept", "application/json");
InputStream responseStream = connection.getInputStream();
ObjectMapper mapper = new ObjectMapper();
JsonNode root = mapper.readTree(responseStream);
System.out.println(root.path("fact").asText()); */

public class ThesaurusAPI {
    public static void main(String[] args) {
        try {
            // Define the API URL
            String word = "elegant"; // Change this word as needed
            URI uri = new URI("https", "api.api-ninjas.com", "/v1/thesaurus", "word=" + word, null);
            URL url = uri.toURL();

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("X-Api-Key", "D6orPYeYSShY46WUTURS3Q==igNLcXHXWXAHMoG3"); // Replace with your actual API key

            // Get response stream
            InputStream responseStream = connection.getInputStream();

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseStream);

            // Extract synonyms (assuming API returns a "synonyms" array)
            JsonNode synonyms = root.path("synonyms");
            if (synonyms.isArray()) {
                System.out.println("Synonyms for '" + word + "':");
                for (JsonNode synonym : synonyms) {
                    System.out.println("- " + synonym.asText());
                }
            } else {
                System.out.println("No synonyms found.");
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
