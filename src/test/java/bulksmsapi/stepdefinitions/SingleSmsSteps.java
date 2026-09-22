package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class SingleSmsSteps {

    private final Map<String, String> body = new LinkedHashMap<>();

    @Given("a single SMS request to {string} with message {string}")
    public void aSingleSmsRequestTo(String to, String message) {
        body.clear();
        body.put("to", to);
        body.put("message", message);
        body.put("senderId", "MYAPP");
    }

    @Given("a single SMS request with no {string} field")
    public void aSingleSmsRequestWithNoField(String missingField) {
        body.clear();
        body.put("to", "254712345678");
        body.put("message", "Hello from the automated suite");
        body.put("senderId", "MYAPP");
        body.remove(missingField);
    }

    @Given("a single SMS request to {string} with message {string} and no senderId")
    public void aSingleSmsRequestWithNoSenderId(String to, String message) {
        body.clear();
        body.put("to", to);
        body.put("message", message);
        // senderId intentionally omitted - SingleSmsRequest.senderId is optional
        // and services.send_single falls back to settings.default_sender_id.
    }

    @When("I submit the single SMS request")
    public void iSubmitTheSingleSmsRequest() {
        Response resp = ApiClient.signedJsonSpec(body)
                .body(JsonUtil.toJsonObject(body))
                .post("/api/v1/sms");
        ResponseContext.set(resp);
    }

    @When("I submit the single SMS request with an invalid signature")
    public void iSubmitTheSingleSmsRequestWithAnInvalidSignature() {
        Response resp = ApiClient.badlySignedJsonSpec()
                .body(JsonUtil.toJsonObject(body))
                .post("/api/v1/sms");
        ResponseContext.set(resp);
    }
}
