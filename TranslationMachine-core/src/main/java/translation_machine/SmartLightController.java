package translation_machine;

import translation_machine.TuyaService;
import java.util.Arrays;

public class SmartLightController {

    private static final String BULB1 = "ebf8593c0cef7c597clxzv";
    private static final String BULB2 = "eb1ff9063aa1400c38l60d";
    private static final String BULB3 = "ebf8593c0cef7c597clxzv";

    private static long lastUpdate = 0;
    private static final long MIN_GAP = 600;

    private static long lastBatchUpdate = 0;
    private static final long BATCH_GAP = 250; // ms (adjust 150–500 depending on responsiveness)

    public static void init() {
        try {
            TuyaService.getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setColor(String deviceId, String hex) {

        try {
            int[] hsv = hexToHSV(hex);

            String body = "{ \"commands\": [" +
                    "{\"code\":\"switch_led\",\"value\":true}," +
                    "{\"code\":\"colour_data_v2\",\"value\":{" +
                    "\"h\":" + hsv[0] + "," +
                    "\"s\":" + hsv[1] + "," +
                    "\"v\":" + hsv[2] +
                    "}}" +
                    "]}";

            TuyaService.sendCommand(deviceId, body);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (System.currentTimeMillis() - lastUpdate < MIN_GAP) {
            try { Thread.sleep(200); } catch (Exception ignored) {}
        }

        lastUpdate = System.currentTimeMillis();
    }

    public static void turnOffAll() {
        try {
            String body = "{ \"commands\": [{\"code\":\"switch_led\",\"value\":false}]}";

            TuyaService.sendCommand(BULB1, body);
            TuyaService.sendCommand(BULB2, body);
            TuyaService.sendCommand(BULB3, body);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAllColors(String[] colors) {

        System.out.println("LIGHT CALL RECEIVED: " + Arrays.toString(colors));

        if (colors == null || colors.length < 3) return;

        // simple guard ONLY (no threads, no sleeps)
        long now = System.currentTimeMillis();
        if (now - lastUpdate < MIN_GAP) return;
        lastUpdate = now;

        setColor(BULB1, colors[0]);
        setColor(BULB2, colors[1]);
        setColor(BULB3, colors[2]);
    }

    private static int[] hexToHSV(String hex) {
        int r = Integer.valueOf(hex.substring(1, 3), 16);
        int g = Integer.valueOf(hex.substring(3, 5), 16);
        int b = Integer.valueOf(hex.substring(5, 7), 16);

        float[] hsv = java.awt.Color.RGBtoHSB(r, g, b, null);

        return new int[]{
                (int)(hsv[0] * 360),
                (int)(hsv[1] * 1000),
                (int)(hsv[2] * 1000)
        };
    }

}