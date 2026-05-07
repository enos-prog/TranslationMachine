package translation_machine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class TextToColor {

    private final String apiKey = "p93Y86bTFE930QynAMzwdm8BtdL65VttGK2CAM8D";

    /**
     * Fetches a color for the given text using the OneSimpleAPI Color API.
     *
     * @param text the input text to generate a color for
     * @return a String array: [color name, hex code]
     */
    public String[] getColorForText(String text) {
        try {
            // Encode the text to handle special characters like &, ?, /
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            // Build URI (removed double ampersand &&)
            URI uri = new URI(
                    "https://onesimpleapi.com/api/color?token="
                            + apiKey
                            + "&output=json&text=" + encodedText
            );

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("API request failed. HTTP code: " + responseCode);
                return new String[]{"Unknown", "#FFFFFF"};
            }

            // Read response
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON
            JSONObject json = new JSONObject(response.toString());
            String hex = json.optString("hex", "#FFFFFF");    // default fallback
            String name = json.optString("color", "Unknown"); // default fallback

            return new String[]{name, hex};

        } catch (Exception e) {
            System.out.println("Error fetching color: " + e.getMessage());
            return new String[]{"Unknown", "#FFFFFF"};
        }
    }
}