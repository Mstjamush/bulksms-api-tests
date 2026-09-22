package bulksmsapi.stepdefinitions;

import bulksmsapi.config.Config;
import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tests the login endpoint itself - see AdminSession for the "just get me a token" helper
 * used as a precondition by other feature files. */
public class AdminAuthSteps {

    private final Map<String, String> body = new LinkedHashMap<>();

    @Given("admin login credentials {string} and {string}")
    public void adminLoginCredentials(String email, String password) {
        body.clear();
        body.put("email", email);
        body.put("password", password);
    }

    @Given("the configured super admin credentials")
    public void theConfiguredSuperAdminCredentials() {
        body.clear();
        body.put("email", Config.get("admin.super_admin.email"));
        body.put("password", Config.get("admin.super_admin.password"));
    }

    @Given("the configured super admin email with password {string}")
    public void theConfiguredSuperAdminEmailWithPassword(String password) {
        body.clear();
        body.put("email", Config.get("admin.super_admin.email"));
        body.put("password", password);
    }

    @Given("admin login credentials with no email and password {string}")
    public void adminLoginCredentialsWithNoEmail(String password) {
        body.clear();
        body.put("password", password);
    }

    @When("I log in to the admin API")
    public void iLogInToTheAdminApi() {
        Response resp = ApiClient.adminUnauthSpec()
                .body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/auth/login");
        ResponseContext.set(resp);
        if (resp.statusCode() == 200) {
            ApiClient.setAdminToken(resp.jsonPath().getString("token"));
        }
    }
}
