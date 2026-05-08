package translation_machine;

//import com.lowlevelsubmarine.genius.GLA;
//import com.lowlevelsubmarine.genius.Song;
import core.GLA;
import genius.SongSearch;
import translation_machine.InteractiveMusic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static translation_machine.LyricAnalyzer.findTopSubstrings;
import static translation_machine.LyricAnalyzer.printSubstrings;

import core.GLA;
import genius.SongSearch;

import java.util.Arrays;
import java.util.List;

public class TranslationMachine {

    private final GLA gla;

    public TranslationMachine() {
        this.gla = new GLA();
    }

    public GLA getGla() {
        return gla;
    }

   public SongResult analyzeSong(String songTitle, String artistName) {

        try {

            SongSearch search = new SongSearch(gla, songTitle);

            String lyrics = null;

            for (SongSearch.Hit hit : search.getHits()) {

                if (hit.getArtist() == null ||
                    hit.getArtist().getName() == null) continue;

                if (hit.getArtist().getName()
                        .equalsIgnoreCase(artistName)) {

                    try {
                        lyrics = hit.fetchLyrics();
                    } catch (Exception e) {
                        return null;
                    }

                    break;
                }
            }

            if (lyrics == null || lyrics.isBlank()) {
                return null;
            }

            // ---------------- RAW ----------------
            List<String> raw =
                    LyricAnalyzer.findTopSubstrings(lyrics);

            // ---------------- FILTER ----------------
            List<String> filtered =
                    SubstringFilter.filter(raw);

            // ---------------- FINAL TOP 3 ----------------
            List<String> top3 = new ArrayList<>();

            for (String s : filtered) {

                if (top3.size() >= 3) break;

                top3.add(s);
            }

            // fallback additions ONLY if different
            /* for (String candidate : raw) {

                if (top3.size() >= 3) break;

                boolean duplicate = false;

                for (String existing : top3) {

                    double similarity =
                            SubstringFilter.tokenSimilarity(
                                    candidate,
                                    existing
                            );

                    if (similarity >= 0.25) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    top3.add(candidate);
                }
            } */

           for (String candidate : raw) {

                if (top3.size() >= 3) break;

                if (!top3.contains(candidate)) {
                    top3.add(candidate);
                }
            }

            // safety fill
            while (top3.size() < 3) {
                top3.add("No dominant theme");
            }

            // ---------------- COLORS ----------------
            String[] colors = new String[3];

            TextToColor textToColor = new TextToColor();

            for (int i = 0; i < 3; i++) {

                try {

                    String main =
                            MainIdea.getMainIdeaFromAPI(top3.get(i));

                    String[] colorData =
                            textToColor.getColorForText(main);

                    colors[i] =
                            (colorData != null && colorData.length > 1)
                                    ? colorData[1]
                                    : "#CCCCCC";

                } catch (Exception e) {

                    colors[i] = "#CCCCCC";
                }
            }

            String honorable =
                    LyricAnalyzer.getHonorableMention(lyrics);

            return new SongResult(
                    songTitle,
                    artistName,
                    top3,
                    honorable,
                    colors
            );

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    // OPTIONAL LEGACY METHOD (unchanged logic, just cleaned)
    public void searchForSongs(String[] songTitles, String[] artistNames) {

        TextToColor textToColor = new TextToColor();

        try {
            GLA geniusApi = new GLA();

            for (int i = 0; i < songTitles.length; i++) {

                String songTitle = songTitles[i];
                String artistName = artistNames[i];

                int page = 1;
                int resultsChecked = 0;
                boolean found = false;

                while (resultsChecked < 200) {

                    SongSearch searchResult = new SongSearch(geniusApi, songTitle, page);

                    if (searchResult.getHits() == null || searchResult.getHits().isEmpty()) {
                        break;
                    }

                    for (SongSearch.Hit hit : searchResult.getHits()) {

                        resultsChecked++;

                        if (hit.getArtist() != null &&
                            hit.getArtist().getName().equalsIgnoreCase(artistName)) {

                            String lyrics;

                            try {
                                lyrics = hit.fetchLyrics();
                            } catch (Exception e) {
                                continue;
                            }

                            if (lyrics == null || lyrics.isBlank()) {
                                continue;
                            }

                            found = true;

                            //List<String> topSubstrings = LyricAnalyzer.findTopSubstrings(lyrics);
                            String honorableMention = LyricAnalyzer.getHonorableMention(lyrics);

                            System.out.println("\nMain Ideas for '" + songTitle + "' by " + artistName);

                            List<String> raw = LyricAnalyzer.findTopSubstrings(lyrics);
                            List<String> filtered = SubstringFilter.filter(raw);

                            List<String> top3 = new ArrayList<>(filtered);

                            /* while (top3.size() < 3 && top3.size() < raw.size()) {
                                top3.add(raw.get(top3.size()));
                            } */
                            
                           while (top3.size() < 3 && i < raw.size()) {

                                String candidate = raw.get(i);

                                boolean duplicate = false;

                                for (String existing : top3) {

                                    double similarity =
                                            SubstringFilter.tokenSimilarity(candidate, existing);

                                    if (similarity >= 0.25) {
                                        duplicate = true;
                                        break;
                                    }
                                }

                                if (!duplicate) {
                                    top3.add(candidate);
                                }

                                i++;
                            }

                            //for (String substring : LyricAnalyzer.findTopSubstrings(lyrics)) 
                            for (String substring : top3) {

                                String mainIntent = MainIdea.getMainIdeaFromAPI(substring);
                                String[] colorData = textToColor.getColorForText(mainIntent);

                                System.out.println("\nSubstring: " + substring);
                                System.out.println("Hex: " + colorData[1]);
                            } 

                            if (honorableMention != null && !honorableMention.isEmpty()) {
                                System.out.println("\nHonorable Mention: " + honorableMention);
                            }

                            break;
                        }

                        if (resultsChecked >= 200) break;
                    }

                    if (found) break;

                    page = searchResult.getNextPage();
                    if (page == 0) break;
                }

                if (!found) {
                    System.out.println("No matching song found for " + songTitle);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}