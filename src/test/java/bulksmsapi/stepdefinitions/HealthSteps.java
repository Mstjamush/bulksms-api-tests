package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class HealthSteps {

    @When("I call the health endpoint")
    public void iCallTheHealthEndpoint() {
        Response resp = ApiClient.baseSpec().get("/health");
        ResponseContext.set(resp);
    }
}
