package bulksmsapi.utils;

import io.restassured.RestAssured;

/** Registers ApiLoggingFilter on RestAssured's global filter list exactly
 * once, regardless of which class (ApiClient, TestRailClient) triggers it first. */
public final class HttpLogging {

    private static boolean installed = false;

    private HttpLogging() {
    }

    public static synchronized void install() {
        if (installed) return;
        RestAssured.filters(new ApiLoggingFilter());
        installed = true;
    }
}
