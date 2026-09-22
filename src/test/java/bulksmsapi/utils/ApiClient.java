package bulksmsapi.utils;

import bulksmsapi.config.Config;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.time.Instant;
import java.util.Map;

/**
 * Builds RestAssured request specs against bulksms-api. Two auth schemes,
 * matching the two halves of the API (see bulksms-api/README.md):
 *  - signed*Spec(): X-Api-Key/X-Timestamp/X-Signature, for the public
 *    ingestion endpoints under /api/v1/sms.
 *  - admin*Spec(): JWT bearer token, for /api/v1/admin.
 */
public final class ApiClient {

    static {
        HttpLogging.install();
    }

    private static String adminToken;

    private ApiClient() {
    }

    public static RequestSpecification baseSpec() {
        return RestAssured.given().baseUri(Config.get("api.base.url"));
    }

    /** Correctly signed spec for a flat JSON body - pass the exact fields being sent. */
    public static RequestSpecification signedJsonSpec(Map<String, String> body) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String apiKeyHeader = SignatureUtil.apiKeyFor(Config.get("auth.api_key"), timestamp);
        String signature = SignatureUtil.signatureFor(body, apiKeyHeader);

        return baseSpec()
                .contentType(ContentType.JSON)
                .header("X-Timestamp", timestamp)
                .header("X-Api-Key", apiKeyHeader)
                .header("X-Signature", signature);
    }

    /** Same as signedJsonSpec but with a deliberately wrong signature, for negative auth tests. */
    public static RequestSpecification badlySignedJsonSpec() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String apiKeyHeader = SignatureUtil.apiKeyFor(Config.get("auth.api_key"), timestamp);

        return baseSpec()
                .contentType(ContentType.JSON)
                .header("X-Timestamp", timestamp)
                .header("X-Api-Key", apiKeyHeader)
                .header("X-Signature", "0000000000000000000000000000000");
    }

    /** The bulk multipart endpoint has no JSON body to sign - only X-Api-Key/X-Timestamp apply. */
    public static RequestSpecification signedMultipartSpec() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String apiKeyHeader = SignatureUtil.apiKeyFor(Config.get("auth.api_key"), timestamp);

        return baseSpec()
                .header("X-Timestamp", timestamp)
                .header("X-Api-Key", apiKeyHeader);
    }

    public static RequestSpecification adminUnauthSpec() {
        return baseSpec().contentType(ContentType.JSON);
    }

    public static RequestSpecification adminAuthedSpec() {
        if (adminToken == null || adminToken.isBlank()) {
            throw new IllegalStateException("No admin token - log in first (see AdminSession/AdminAuthSteps)");
        }
        return baseSpec().contentType(ContentType.JSON).header("Authorization", "Bearer " + adminToken);
    }

    public static void setAdminToken(String token) {
        adminToken = token;
    }
}
