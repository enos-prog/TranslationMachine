package translation_machine;

import java.util.*;

public class LyricAnalyzer {

    // Modify this method to return a List of top substrings and set an honorable mention based on frequency.
    public static List<String> findTopSubstrings(String lyrics) {
        Map<String, Integer> substringCounts = new HashMap<>();
        List<String> topSubstrings = new ArrayList<>();

        // Split lyrics into lines and count occurrences
        for (String line : lyrics.split("\n")) {
            line = line.trim();
            if (!line.isEmpty() && !line.matches(".*[\\[\\]].*")) { // Exclude empty and bracketed lines
                substringCounts.put(line, substringCounts.getOrDefault(line, 0) + 1);
            }
        }

        // Sort substrings by frequency (descending)
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(substringCounts.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue())); // Sort by frequency

        // Get the top 3 substrings and handle honorable mention if frequency ties occur
        int count = 0;
        int thirdHighestFrequency = 0;

        // Collect the top 3 substrings
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            if (count < 3) {
                topSubstrings.add(entry.getKey());
                thirdHighestFrequency = entry.getValue();
                count++;
            } else if (entry.getValue() == thirdHighestFrequency) {
                // If frequency equals third highest, this is an honorable mention
                topSubstrings.add(entry.getKey());
            } else {
                break; // Stop processing if frequency is lower
            }
        }

        return topSubstrings;
    }

    // Return the honorable mention after top substrings are determined.
    public static String getHonorableMention(String lyrics) {
        List<String> topSubstrings = findTopSubstrings(lyrics);
        if (topSubstrings.size() > 3) {
            // Honorable mention is the next one after the top 3 substrings
            return topSubstrings.get(3);
        }
        return null; // No honorable mention if fewer than 4 substrings
    }

    public static void printSubstrings(List<String> substrings) {
        for (String substring : substrings) {
            System.out.println(substring);
        }
    }

    // You can also define the estimateTokens method if it's not already present
    public static int estimateTokens(String text) {
        // Estimate tokens by assuming 1 token = 1.33 words
        String[] words = text.split("\\s+");
        return (int) (words.length * 1.33);
    }
}