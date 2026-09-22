package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.AdminFixtures;
import bulksmsapi.utils.AdminRoles;
import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import bulksmsapi.utils.RandomData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdminUsersSteps {

    private static final String DEFAULT_PASSWORD = "QaPass123!";

    private final Map<String, Object> body = new LinkedHashMap<>();
    private String email;

    @Given("a new user for that client with role {string}")
    public void aNewUserForThatClientWithRole(String roleName) {
        newUser(ClientAdminContext.client().clientId(), roleName, DEFAULT_PASSWORD);
    }

    /** client_id omitted - when the caller isn't a super admin it's forced
     * server-side to their own client anyway (see routers.py create_user). */
    @Given("a new user with role {string}")
    public void aNewUserWithRole(String roleName) {
        newUser(null, roleName, DEFAULT_PASSWORD);
    }

    @Given("a new user with no client_id")
    public void aNewUserWithNoClientId() {
        newUser(null, "Digital Administrator", DEFAULT_PASSWORD);
    }

    @Given("a new user with password {string}")
    public void aNewUserWithPassword(String password) {
        newUser(null, "Digital Administrator", password);
    }

    private void newUser(Integer clientId, String roleName, String password) {
        String suffix = RandomData.uniqueSuffix();
        email = "qa.user." + suffix + "@example.com";

        body.clear();
        body.put("email_address", email);
        body.put("full_names", "QA Fixture User");
        body.put("password", password);
        body.put("msisdn", RandomData.uniqueMsisdn());
        body.put("role_id", AdminRoles.idForName(roleName));
        if (clientId != null) body.put("client_id", clientId);
    }

    @When("I create the user")
    public void iCreateTheUser() {
        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/users");
        ResponseContext.set(resp);

        if (resp.statusCode() == 200) {
            String password = String.valueOf(body.get("password"));
            ClientAdminContext.setLastCreatedUser(
                    new AdminFixtures.ProvisionedUser(resp.jsonPath().getInt("user_id"), email, password));
        }
    }
}
