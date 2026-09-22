package bulksmsapi.stepdefinitions;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.junit.Assert;

/** Response assertions shared across every feature file. */
public class CommonSteps {

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expected) {
        Response resp = ResponseContext.get();
        Assert.assertEquals("Unexpected status. Body: " + resp.asString(), expected, resp.statusCode());
    }

    @Then("the response status should be {int} or {int}")
    public void theResponseStatusShouldBeEitherOf(int a, int b) {
        Response resp = ResponseContext.get();
        int actual = resp.statusCode();
        Assert.assertTrue("Expected " + a + " or " + b + " but got " + actual + ". Body: " + resp.asString(),
                actual == a || actual == b);
    }

    @Then("the JSON response should have a non-empty {string} field")
    public void theJsonResponseShouldHaveANonEmptyField(String field) {
        Response resp = ResponseContext.get();
        Object value = resp.jsonPath().get(field);
        Assert.assertNotNull("Missing field '" + field + "'. Body: " + resp.asString(), value);
        Assert.assertFalse("Field '" + field + "' is empty. Body: " + resp.asString(),
                value.toString().trim().isEmpty());
    }

    @Then("the JSON response field {string} should equal {string}")
    public void theJsonResponseFieldShouldEqual(String field, String expected) {
        Response resp = ResponseContext.get();
        String actual = resp.jsonPath().getString(field);
        Assert.assertEquals("Body: " + resp.asString(), expected, actual);
    }
}
