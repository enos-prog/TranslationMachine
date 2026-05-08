package translation_machine;

import java.util.*;

public class SubstringFilter {

    private static final double SIMILARITY_THRESHOLD = 0.25;

    public static List<String> filter(List<String> input) {

        List<String> result = new ArrayList<>();

        System.out.println("\n----- FILTER START -----");
        System.out.println("INPUT:");
        for (String s : input) {
            System.out.println(s);
        }

        for (String candidate : input) {

            boolean tooSimilar = false;

            for (String accepted : result) {

                double similarity = tokenSimilarity(candidate, accepted);

                System.out.println(
                    "\nCOMPARE:\n" +
                    "CANDIDATE: " + candidate + "\n" +
                    "ACCEPTED: " + accepted + "\n" +
                    "SIMILARITY: " + similarity
                );

                if (similarity >= SIMILARITY_THRESHOLD &&
                    !isMeaningfullyDifferent(candidate, accepted)) {

                    System.out.println("REJECTED AS DUPLICATE");

                    tooSimilar = true;
                    break;
                }
            }

            if (!tooSimilar) {

                System.out.println("ACCEPTED: " + candidate);
                result.add(candidate);
            }

            if (result.size() == 3) break; // HARD LIMIT
        }

        System.out.println("\nFINAL FILTERED:");
        for (String s : result) {
            System.out.println(s);
        }
        System.out.println("----- FILTER END -----\n");

        return result;
    }

    public static List<String> ensureSize(List<String> input, List<String> fallback, int size) {

        List<String> result = new ArrayList<>(input);

        for (String f : fallback) {
            if (result.size() >= size) break;
            if (!result.contains(f)) result.add(f);
        }

        return result.subList(0, Math.min(size, result.size()));
    }

    // ---------------- TOKEN SIMILARITY ----------------
    /* private static double tokenSimilarity(String a, String b) {

        Set<String> setA = new HashSet<>(Arrays.asList(a.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")
    .split("\\s+")));
        Set<String> setB = new HashSet<>(Arrays.asList(b.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")
    .split("\\s+")));

        if (setA.isEmpty() || setB.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        return (double) intersection.size() / union.size();
    } */

   public static double tokenSimilarity(String a, String b) {

        Set<String> setA = normalize(a);
        Set<String> setB = normalize(b);

        if (setA.isEmpty() || setB.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        return (double) intersection.size() / union.size();
    }

    private static Set<String> normalize(String text) {

        Set<String> result = new HashSet<>();

        String cleaned = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ]", "");

        cleaned = cleaned.replaceAll("(\\b\\w+\\b)(\\s+\\1)+", "$1");

        List<String> stopWords = Arrays.asList(
            "the","a","an","and","or","but",
            "i","you","me","my","your",
            "to","of","in","on","at",
            "for","is","it","this","that",
            "im","ive","ill","dont","cant",
            "oh","ooh","yeah","uh",
            "woah","shawty","na","la",
            "hey","girl","boy"
        );

        for (String word : cleaned.split("\\s+")) {

            if (word.length() <= 2) continue;

            if (stopWords.contains(word)) continue;

            result.add(word);
        }

        return result;
    }

    // ---------------- SEMANTIC OVERRIDE ----------------
    private static boolean isMeaningfullyDifferent(String a, String b) {

        // simple heuristic version (we can upgrade later with embeddings)

        String aLower = a.toLowerCase();
        String bLower = b.toLowerCase();

        // opposite triggers (expand later with synonym engine)
        List<String[]> opposites = List.of(
            new String[]{"give up", "keep chasing"},
            new String[]{"love", "hate"},
            new String[]{"stay", "leave"},
            new String[]{"mine", "yours"},
            new String[]{"start", "end"}
        );

        for (String[] pair : opposites) {
            boolean aHas = aLower.contains(pair[0]) && bLower.contains(pair[1]);
            boolean bHas = aLower.contains(pair[1]) && bLower.contains(pair[0]);

            if (aHas || bHas) return true;
        }

        return false;
    }

    private static boolean hasUniqueKeyword(String candidate, List<String> accepted) {

        Set<String> acceptedWords = new HashSet<>();

        for (String a : accepted) {
            acceptedWords.addAll(Arrays.asList(a.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")
    .split("\\s+")));
        }

        for (String word : candidate.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")
    .split("\\s+")) {
            if (!acceptedWords.contains(word)) {
                return true;
            }
        }

        return false;
    }
}