package translation_machine;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.*;
import java.security.MessageDigest;
import java.time.Instant;

public class TuyaService {

    private static final String ACCESS_ID = "ghee9urcurjtr45ktgcx";
    private static final String ACCESS_SECRET = "7384e9a60b0b49e68d6a36cde1c2dee6";
    private static final String BASE_URL = "https://openapi.tuyaus.com";

    private static String accessToken = "";

    private static final HttpClient client = HttpClient.newHttpClient();

    private static long tokenTime = 0;
    private static final long TOKEN_TTL = 1000 * 60 * 60; // 1 hour

    // 🔑 STEP 1: GET TOKEN
    public static void getAccessToken() throws Exception {

        String t = String.valueOf(Instant.now().toEpochMilli());

        String method = "GET";
        String uri = "/v1.0/token?grant_type=1";
        String bodyHash = sha256("");

        String stringToSign =
                method + "\n" +
                bodyHash + "\n\n" +
                uri;

        String signStr = ACCESS_ID + t + stringToSign;

        String sign = sign(signStr);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + uri))
                .header("client_id", ACCESS_ID)
                .header("sign", sign)
                .header("t", t)
                .header("sign_method", "HMAC-SHA256")
                .header("sign_version", "2")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        String body = response.body();

        System.out.println("TOKEN RESPONSE: " + body);

        if (!body.contains("access_token")) {
            throw new RuntimeException("Failed to get token: " + body);
        }

        //accessToken =
                //body.split("\"access_token\":\"")[1].split("\"")[0];

        System.out.println("Token: " + accessToken);

        accessToken = body.split("\"access_token\":\"")[1].split("\"")[0];
        tokenTime = System.currentTimeMillis();
    }

    // 🔥 STEP 2: SEND COMMAND
    public static void sendCommand(String deviceId, String jsonBody) throws Exception {

        if (accessToken == null ||
            accessToken.isEmpty() ||
            System.currentTimeMillis() - tokenTime > TOKEN_TTL) {
            getAccessToken();
        }

        String t = String.valueOf(Instant.now().toEpochMilli());

        String bodyHash = sha256(jsonBody);

        String stringToSign =
                "POST\n" +
                bodyHash + "\n\n" +
                "/v1.0/iot-03/devices/" + deviceId + "/commands";

        String signStr = ACCESS_ID + accessToken + t + stringToSign;
        String sign = sign(signStr);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1.0/iot-03/devices/" + deviceId + "/commands"))
                .header("client_id", ACCESS_ID)
                .header("access_token", accessToken)
                .header("sign", sign)
                .header("t", t)
                .header("sign_method", "HMAC-SHA256")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        //client.send(request, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Device: " + deviceId);
        System.out.println("Payload: " + jsonBody);
        System.out.println("Response: " + response.body());

    }

    //  SIGNING
    private static String sign(String str) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(ACCESS_SECRET.getBytes(), "HmacSHA256"));
        byte[] bytes = mac.doFinal(str.getBytes());
        return bytesToHex(bytes).toUpperCase();
    }

    private static String sha256(String str) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(str.getBytes());
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }
        return hex.toString();
    }

}