package bulksmsapi.utils;

import bulksmsapi.config.Config;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shared login helper for scenarios that need an authenticated admin session
 * as a *prerequisite* (as opposed to admin_auth.feature, which tests login itself). */
public final class AdminSession {

    private AdminSession() {
    }

    private static String superAdminToken;

    public static void loginAsSuperAdmin() {
        login(Config.get("admin.super_admin.email"), Config.get("admin.super_admin.password"));
    }

    /** A cached Super Administrator token that doesn't replace the active session. */
    public static synchronized String superAdminToken() {
        if (superAdminToken == null) {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("email", Config.get("admin.super_admin.email"));
            body.put("password", Config.get("admin.super_admin.password"));
            Response resp = ApiClient.adminUnauthSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/auth/login");
            if (resp.statusCode() != 200) {
                throw new IllegalStateException("Super admin login failed (" + resp.statusCode() + "): " + resp.asString());
            }
            superAdminToken = resp.jsonPath().getString("token");
        }
        return superAdminToken;
    }

    public static void login(String email, String password) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);

        Response resp = ApiClient.adminUnauthSpec()
                .body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/auth/login");

        if (resp.statusCode() != 200) {
            throw new IllegalStateException(
                    "Admin login failed (" + resp.statusCode() + "): " + resp.asString()
                    + ". Check admin.super_admin.email/password in testrail.properties"
                    + " - run `python seed_admin.py` in bulksms-api first if you haven't.");
        }
        ApiClient.setAdminToken(resp.jsonPath().getString("token"));
    }
}
