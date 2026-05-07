package translation_machine;

import java.util.Arrays;

public class LightTransitionController {

    private static String[] lastColors = new String[]{"#000000","#000000","#000000"};
    private static boolean busy = false;

    public static void update(String[] colors) {

        if (colors == null || colors.length < 3) return;

        // prevent spam during swipe animation
        if (busy) return;
        busy = true;

        new Thread(() -> {
            try {
                smoothUpdate(colors);
                lastColors = colors;
            } finally {
                busy = false;
            }
        }).start();
    }

    private static void smoothUpdate(String[] colors) {
        try {
            // single-step fade illusion (no blackout step)
            SmartLightController.setAllColors(colors);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}