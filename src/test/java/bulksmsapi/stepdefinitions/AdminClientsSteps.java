package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.AdminFixtures;
import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import bulksmsapi.utils.RandomData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminClientsSteps {

    private final Map<String, String> body = new LinkedHashMap<>();
    private String createdClientEmail;

    @Given("a new client with a unique name")
    public void aNewClientWithAUniqueName() {
        String suffix = RandomData.uniqueSuffix();
        // .test/.example/.invalid TLDs are rejected by email-validator (reserved,
        // see RFC 2606) - .com isn't, so it's used here purely for a syntactically
        // valid, collision-free address (nothing ever sends mail to it).
        createdClientEmail = "qa.client." + suffix + "@example.com";

        body.clear();
        body.put("client_name", "QA Test Client " + suffix);
        body.put("client_email", createdClientEmail);
        body.put("address", "Nairobi");
        body.put("country", "KENYA");
    }

    @Given("a new client with no client_name")
    public void aNewClientWithNoClientName() {
        String suffix = RandomData.uniqueSuffix();
        createdClientEmail = "qa.client." + suffix + "@example.com";

        body.clear();
        body.put("client_email", createdClientEmail);
        body.put("address", "Nairobi");
        body.put("country", "KENYA");
    }

    @Given("a new client with an invalid email address")
    public void aNewClientWithAnInvalidEmailAddress() {
        String suffix = RandomData.uniqueSuffix();

        body.clear();
        body.put("client_name", "QA Test Client " + suffix);
        body.put("client_email", "not-a-valid-email");
        body.put("address", "Nairobi");
        body.put("country", "KENYA");
    }

    @When("I create the client")
    public void iCreateTheClient() {
        Response resp = ApiClient.adminAuthedSpec()
                .body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/clients");
        ResponseContext.set(resp);

        if (resp.statusCode() == 200) {
            ClientAdminContext.setClient(new AdminFixtures.ProvisionedClient(resp.jsonPath().getInt("client_id"), createdClientEmail));
        }
    }

    @When("I list clients")
    public void iListClients() {
        Response resp = ApiClient.adminAuthedSpec().get("/api/v1/admin/clients");
        ResponseContext.set(resp);
    }

    @Then("the client list should include the client created earlier in this scenario")
    public void theClientListShouldIncludeTheClientCreatedEarlier() {
        Response resp = ResponseContext.get();
        List<String> emails = resp.jsonPath().getList("client_email", String.class);
        Assert.assertTrue("Expected " + createdClientEmail + " in client list: " + resp.asString(),
                emails != null && emails.contains(createdClientEmail));
    }
}
