package translation_machine;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.io.IOException;
import java.util.*;

import org.json.JSONObject;

public class MainIdea {
    private static final String API_URL = "https://api.nlpcloud.io/v1/gpu/finetuned-llama-3-70b/intent-classification";
    private static final String API_TOKEN = "971c737561bb63fe1c8a14b9f3914beb5ae02138"; // Replace with your actual API token
    public static final int MAX_TOKENS = 128000; // Maximum allowed tokens for the API

    public static void main(String[] args) {
//        // Sample songs and their lyrics
        Map<String, String> songLyricsMap = new HashMap<>();
        songLyricsMap.put("Toxic", "Uh, the moshpit real toxic\nI got too much profit");
        songLyricsMap.put("Up to You", "I'm gon' leave it up to you\nWhat the hell, babe?");
        songLyricsMap.put("Luv Bad Bitches", "I like good girls, I like good girls\nBad bitches get everything");

        // Process each song
        for (Map.Entry<String, String> entry : songLyricsMap.entrySet()) {
            String songTitle = entry.getKey();
            String lyrics = entry.getValue();

            // Retrieve the top substrings for the song
            List<String> topSubstringsList = LyricAnalyzer.findTopSubstrings(lyrics);
            String[] topSubstrings = topSubstringsList.toArray(new String[0]);

            // Get the honorable mention for the song
            String honorableMention = LyricAnalyzer.getHonorableMention(lyrics);

            // Print the main ideas for each song
            System.out.println("Main Ideas for '" + songTitle + "':\n");
            processSubstrings(topSubstrings, honorableMention);
        }
    }

    // Method to process substrings and output the main idea for each
    public static void processSubstrings(String[] topSubstrings, String honorableMention) {
        // Process the top substrings and retrieve intents
        for (String substring : topSubstrings) {
            try {
                // Truncate the substring if necessary
                String truncatedSubstring = truncateInput(substring, MAX_TOKENS);

                // Get the intent (main idea) for each substring
                String mainIntent = getMainIdeaFromAPI(truncatedSubstring);

                // Print the substring and its intent
                System.out.println("Substring: " + substring);
                System.out.println("Color Description: " + mainIntent);
                System.out.println("Hex Value: #808080\n");  // Example hex color for the description

                // Add a delay between requests to avoid rate-limiting
                try {
                    Thread.sleep(1000);  // Adjust the delay as needed (1000 ms = 1 second)
                } catch (InterruptedException e) {
                    System.out.println("Thread was interrupted while sleeping: " + e.getMessage());
                    Thread.currentThread().interrupt();  // Restore the interrupted status
                }

            } catch (Exception e) {  // Catch all exceptions, not just IOException
                System.out.println("Error processing substring: " + e.getMessage());
            }
        }

        // Process the honorable mention if it exists (only once after all substrings)
        if (honorableMention != null && !honorableMention.isEmpty()) {
            try {
                // Truncate the honorable mention if necessary
                String truncatedHonorableMention = truncateInput(honorableMention, MAX_TOKENS);

                // Get the intent for the honorable mention
                String honorableIntent = getMainIdeaFromAPI(truncatedHonorableMention);

                // Print the honorable mention and its intent only once
                System.out.println("Honorable Mention: " + honorableMention);
                System.out.println("Color Description: " + honorableIntent);
                System.out.println("Hex Value: #FF69B4\n");

            } catch (Exception e) {  // Catch all exceptions for the honorable mention
                System.out.println("Error processing honorable mention: " + e.getMessage());
            }
        }
    }

    // Method to estimate the number of tokens in a given text
    public static int estimateTokens(String text) {
        // Estimate tokens by assuming 1 token = 1.33 words
        String[] words = text.split("\\s+");
        return (int) (words.length * 1.33);
    }

    // Method to truncate input if it exceeds the maximum token limit
    public static String truncateInput(String input, int maxTokens) {
        // Estimate tokens in the input
        int tokens = estimateTokens(input);

        // If the input exceeds the max token limit, truncate it
        if (tokens > maxTokens) {
            String[] words = input.split("\\s+");
            int wordLimit = (int) (maxTokens / 1.33);  // Adjust for token size
            StringBuilder truncatedInput = new StringBuilder();
            for (int i = 0; i < wordLimit; i++) {
                truncatedInput.append(words[i]).append(" ");
            }
            return truncatedInput.toString().trim();
        }
        return input;
    }

    // Method to get the main idea (intent) of a text from the API
    public static String getMainIdeaFromAPI(String text) {
        HttpURLConnection connection = null;
        try {
            URI uri = new URI(API_URL);
            URL url = uri.toURL();

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Add this header for JSON content:
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Token " + API_TOKEN);
            connection.setDoOutput(true);

            String jsonPayload = "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}";

            // Write JSON payload and flush
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();  // flush is good practice
            }

            // Read response
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line.trim());
                }
                JSONObject jsonResponse = new JSONObject(response.toString());

                return jsonResponse.getString("intent");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing API request: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}