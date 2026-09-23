package bulksmsapi.utils;

import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test-data setup layered on the admin API itself - there's no other way to
 * create a client, a client administrator, or a sub-user than through these
 * same endpoints, so scenarios that need one as a *precondition* provision it
 * here rather than duplicating request-building per step class. Each method
 * assumes ApiClient's current admin token already has permission for the
 * call it makes - see call sites (typically AdminCommonSteps).
 */
public final class AdminFixtures {

    private AdminFixtures() {
    }

    public record ProvisionedClient(int clientId, String clientEmail) {
    }

    public record ProvisionedUser(int userId, String email, String password) {
    }

    /** Caller must already be authenticated as the super admin (require_super_admin). */
    public static ProvisionedClient createClient() {
        String suffix = RandomData.uniqueSuffix();
        String email = "qa.client." + suffix + "@example.com";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_name", "QA Fixture Client " + suffix);
        body.put("client_email", email);
        body.put("address", "Nairobi");
        body.put("country", "KENYA");

        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/clients");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: create client failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return new ProvisionedClient(resp.jsonPath().getInt("client_id"), email);
    }

    /**
     * Credits the client's wallet (as the super admin, whatever the active
     * session is). New clients start at zero and billing is enforced, so any
     * scenario that sends needs this first.
     */
    public static void topUp(int clientId, String amountKes) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amountKes);
        body.put("reference", "QA-" + RandomData.uniqueSuffix());
        body.put("note", "Automated suite credit");
        Response resp = ApiClient.superAdminSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/billing/clients/" + clientId + "/topup");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: top-up failed (" + resp.statusCode() + "): " + resp.asString());
        }
    }

    /** Caller must be the client administrator; returns the new sender's short code. */
    public static String createSender() {
        String shortCode = "QA" + RandomData.uniqueSuffix().toUpperCase();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("short_code", shortCode);
        body.put("send_type_id", 1);
        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/senders");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: create sender failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return shortCode;
    }

    /**
     * Caller must already be authenticated as an identity allowed to create
     * users (super admin, or - for their own client - a client administrator).
     * clientId is only honoured when the caller is the super admin; a client
     * administrator's own client is enforced server-side regardless of what's
     * sent, so pass null for that case.
     */
    public static ProvisionedUser createUser(Integer clientId, int roleId) {
        String suffix = RandomData.uniqueSuffix();
        String email = "qa.user." + suffix + "@example.com";
        String password = "QaPass123!";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email_address", email);
        body.put("full_names", "QA Fixture User");
        body.put("password", password);
        body.put("msisdn", RandomData.uniqueMsisdn());
        body.put("role_id", roleId);
        if (clientId != null) body.put("client_id", clientId);

        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/users");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: create user failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return new ProvisionedUser(resp.jsonPath().getInt("user_id"), email, password);
    }
}
