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

    @Then("the JSON response field {string} should be the number {double}")
    public void theJsonResponseFieldShouldBeTheNumber(String field, double expected) {
        Response resp = ResponseContext.get();
        Assert.assertNotNull("Missing field '" + field + "'. Body: " + resp.asString(), resp.jsonPath().get(field));
        Assert.assertEquals("Field '" + field + "'. Body: " + resp.asString(), expected,
                resp.jsonPath().getDouble(field), 0.0001);
    }

    @Then("the JSON response field {string} should be at least {double}")
    public void theJsonResponseFieldShouldBeAtLeast(String field, double minimum) {
        Response resp = ResponseContext.get();
        double actual = resp.jsonPath().getDouble(field);
        Assert.assertTrue("Field '" + field + "' is " + actual + ", expected >= " + minimum + ". Body: " + resp.asString(),
                actual >= minimum);
    }

    @Then("the JSON response should be a list of at least {int} item(s)")
    public void theJsonResponseShouldBeAListOfAtLeast(int minimum) {
        Response resp = ResponseContext.get();
        java.util.List<Object> list = resp.jsonPath().getList("$");
        Assert.assertNotNull("Not a JSON list. Body: " + resp.asString(), list);
        Assert.assertTrue("Expected >= " + minimum + " items, got " + list.size(), list.size() >= minimum);
    }

    @Then("the JSON response should be a list of exactly {int} item(s)")
    public void theJsonResponseShouldBeAListOfExactly(int expected) {
        Response resp = ResponseContext.get();
        java.util.List<Object> list = resp.jsonPath().getList("$");
        Assert.assertNotNull("Not a JSON list. Body: " + resp.asString(), list);
        Assert.assertEquals("List size. Body: " + resp.asString(), expected, list.size());
    }

    @Then("the response header {string} should be a number of at least {int}")
    public void theResponseHeaderShouldBeANumber(String header, int minimum) {
        Response resp = ResponseContext.get();
        String value = resp.header(header);
        Assert.assertNotNull("Missing header " + header, value);
        Assert.assertTrue(header + "=" + value + ", expected >= " + minimum, Integer.parseInt(value.trim()) >= minimum);
    }

    @Then("the response header {string} should equal {string}")
    public void theResponseHeaderShouldEqual(String header, String expected) {
        Assert.assertEquals("Header " + header, expected, ResponseContext.get().header(header));
    }

    @Then("the response body should contain {string}")
    public void theResponseBodyShouldContain(String text) {
        Response resp = ResponseContext.get();
        Assert.assertTrue("Body does not contain '" + text + "': " + resp.asString(), resp.asString().contains(text));
    }

    @Then("the JSON response field {string} should equal {string}")
    public void theJsonResponseFieldShouldEqual(String field, String expected) {
        Response resp = ResponseContext.get();
        String actual = resp.jsonPath().getString(field);
        Assert.assertEquals("Body: " + resp.asString(), expected, actual);
    }
}
