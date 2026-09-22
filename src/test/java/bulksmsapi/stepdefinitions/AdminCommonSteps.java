package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.AdminFixtures;
import bulksmsapi.utils.AdminRoles;
import bulksmsapi.utils.AdminSession;
import bulksmsapi.utils.ApiClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/** Shared admin-session preconditions and generic authorization-failure
 * checks, used across multiple admin feature files so this logic (and the
 * client/client-administrator provisioning it does) isn't duplicated. */
public class AdminCommonSteps {

    @Given("I am authenticated as the super admin")
    public void iAmAuthenticatedAsTheSuperAdmin() {
        AdminSession.loginAsSuperAdmin();
    }

    /** Provisions a brand new client and a Client Administrator for it (via
     * the super admin), then switches the active session to that new user -
     * everything after this step runs as a plain Client Administrator, not
     * the super admin. */
    @Given("I am authenticated as a client administrator for a new client")
    public void iAmAuthenticatedAsAClientAdministratorForANewClient() {
        AdminSession.loginAsSuperAdmin();
        AdminFixtures.ProvisionedClient client = AdminFixtures.createClient();
        AdminFixtures.ProvisionedUser clientAdmin = AdminFixtures.createUser(client.clientId(), AdminRoles.CLIENT_ADMIN);
        ClientAdminContext.setClient(client);
        AdminSession.login(clientAdmin.email(), clientAdmin.password());
    }

    /** Switches the active session to whichever user AdminUsersSteps created last in this scenario. */
    @Given("I log in as the user created above")
    public void iLogInAsTheUserCreatedAbove() {
        AdminFixtures.ProvisionedUser user = ClientAdminContext.lastCreatedUser();
        AdminSession.login(user.email(), user.password());
    }

    @When("I request {string} without a token")
    public void iRequestWithoutAToken(String path) {
        Response resp = ApiClient.adminUnauthSpec().get(path);
        ResponseContext.set(resp);
    }

    @When("I request {string} with a malformed authorization header")
    public void iRequestWithAMalformedAuthorizationHeader(String path) {
        Response resp = ApiClient.baseSpec()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjpwYXNz") // wrong scheme entirely, not just a bad token
                .get(path);
        ResponseContext.set(resp);
    }

    @When("I request {string} with an invalid token")
    public void iRequestWithAnInvalidToken(String path) {
        Response resp = ApiClient.baseSpec()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer not-a-real-jwt-token")
                .get(path);
        ResponseContext.set(resp);
    }

    @When("I GET {string}")
    public void iGet(String path) {
        Response resp = ApiClient.adminAuthedSpec().get(path);
        ResponseContext.set(resp);
    }
}
