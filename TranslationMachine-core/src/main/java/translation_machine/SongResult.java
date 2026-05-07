package translation_machine;

import java.util.List;

public class SongResult {

    private final String songTitle;
    private final String artistName;
    private final List<String> substrings;   // top 3
    private final String honorableMention;   // #4
    private final String[] colors;           // hex values

    public SongResult(String songTitle,
                      String artistName,
                      List<String> substrings,
                      String honorableMention,
                      String[] colors) {

        this.songTitle = songTitle;
        this.artistName = artistName;
        this.substrings = substrings;
        this.honorableMention = honorableMention;
        this.colors = colors;
    }

    // -------- GETTERS --------
    public String getSongTitle() {
        return songTitle;
    }

    public String getArtistName() {
        return artistName;
    }

    public List<String> getSubstrings() {
        return substrings;
    }

    public String getHonorableMention() {
        return honorableMention;
    }

    public String[] getColors() {
        return colors;
    }
}