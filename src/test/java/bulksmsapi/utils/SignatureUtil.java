package bulksmsapi.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Java port of bulksms-api's app/security.py signing scheme
 * (X-Api-Key = MD5(SHA1(AUTH_API_KEY + timestamp)); X-Signature =
 * MD5(sortedCanonicalPayload + X-Api-Key)), so this suite can exercise the
 * real signed path instead of only relying on AUTH_TEST_MODE=true bypassing
 * it. Only handles flat string-valued JSON bodies (a straight port of
 * hash_create's scalar branch) - that covers every payload this suite signs
 * (SingleSmsRequest has no nested fields).
 */
public final class SignatureUtil {

    private SignatureUtil() {
    }

    public static String apiKeyFor(String authApiKey, String timestamp) {
        return md5(sha1(authApiKey + timestamp));
    }

    /** Only the JSON keys actually present in the body being sent should be passed in. */
    public static String signatureFor(Map<String, String> flatBody, String apiKey) {
        TreeMap<String, String> sorted = new TreeMap<>(flatBody);
        StringBuilder hashkey = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            hashkey.append("&").append(entry.getKey()).append("=")
                    .append(entry.getValue() == null ? "null" : entry.getValue());
        }
        String canonical = hashkey.length() > 0 ? hashkey.substring(1) : "";
        return md5(canonical + apiKey);
    }

    public static String md5(String value) {
        return digest("MD5", value);
    }

    public static String sha1(String value) {
        return digest("SHA-1", value);
    }

    private static String digest(String algorithm, String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
