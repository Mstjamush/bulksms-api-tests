package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;

/** The public single-send API called as a known, billed client (X-Api-Client). */
public class PublicClientApiSteps {

    @Given("I know my API client id")
    public void iKnowMyApiClientId() {
        Response resp = ApiClient.adminAuthedSpec().get("/api/v1/admin/api-credentials");
        Assert.assertEquals("api-credentials: " + resp.asString(), 200, resp.statusCode());
        ScenarioData.put("apiClientId", resp.jsonPath().getInt("api_client_id"));
    }

    private void send(int apiClientId, String to, String message, String senderId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("to", to);
        body.put("message", message);
        if (senderId != null) body.put("senderId", senderId);
        ResponseContext.set(ApiClient.clientApiSpec(apiClientId).body(JsonUtil.toJsonObject(body)).post("/api/v1/sms"));
    }

    @When("I send a billed SMS to {string} with message {string} from my sender")
    public void iSendABilledSmsFromMySender(String to, String message) {
        send(ScenarioData.get("apiClientId"), to, message, ClientAdminContext.lastSenderShortCode());
    }

    @When("I send a billed SMS to {string} with message {string} and no sender")
    public void iSendABilledSmsWithNoSender(String to, String message) {
        send(ScenarioData.get("apiClientId"), to, message, null);
    }

    @When("I send a billed SMS to {string} with message {string} from sender {string}")
    public void iSendABilledSmsFromSender(String to, String message, String sender) {
        send(ScenarioData.get("apiClientId"), to, message, sender);
    }

    @When("I send an SMS as unknown API client {int}")
    public void iSendAnSmsAsUnknownApiClient(int apiClientId) {
        send(apiClientId, "254712345678", "Hello", "MYAPP");
    }

    @When("I send a signed SMS using the {string} field for the number {string}")
    public void iSendASignedSmsUsingTheFieldForTheNumber(String field, String number) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put(field, number);
        body.put("message", "Alias field check");
        body.put("senderId", "MYAPP");
        ResponseContext.set(ApiClient.signedJsonSpec(body).body(JsonUtil.toJsonObject(body)).post("/api/v1/sms"));
    }
}
