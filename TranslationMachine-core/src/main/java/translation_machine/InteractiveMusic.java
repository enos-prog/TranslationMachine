package translation_machine;

import java.util.List;

public interface InteractiveMusic {

    /* Method 1: most frequent words/phrases from songs */

    public List<String> mainIdea();

    /* Method 2: searches for a synonym/singular word to represent input */

    public String synonymFinder();

    /* Method 3: searches for word that best describes artist inputted */

    public String artistSynonym();

    /* Method 4: compares strings from 1, to retrieve 1 color */

    public String color();

    /* Method 5: uses color and strings to from 1 and 4 to find location */

    public String bestLocation();

    /* Method 6: compares bpm of songs (finds avg.) and avg bpm from artists to come up with action */

    public String bpmAction();

    /* Method 7: to string of all data, summary outputted with all other methods info */

    public String summary();

    /* Method 8: to string blurb based off of inputs */

    public String blurb();


}
