package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import bulksmsapi.utils.RandomData;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;

/** Message templates and the placeholder preview. */
public class AdminTemplatesSteps {

    @When("I create a template with body {string}")
    public void iCreateATemplateWithBody(String body) {
        String name = "QA template " + RandomData.uniqueSuffix();
        ScenarioData.put("templateName", name);
        post(name, body);
    }

    @When("I create another template with the same name")
    public void iCreateAnotherTemplateWithTheSameName() {
        post(ScenarioData.get("templateName"), "Different body");
    }

    private void post(String name, String body) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("name", name);
        req.put("body", body);
        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(req)).post("/api/v1/admin/templates");
        ResponseContext.set(resp);
        if (resp.statusCode() == 200) ScenarioData.put("templateId", resp.jsonPath().getInt("id"));
    }

    @When("I preview the message {string} against that broadcast list")
    public void iPreviewTheMessageAgainstThatBroadcastList(String body) {
        preview(body, List.of(ScenarioData.<Integer>get("listId")));
    }

    @When("I preview the message {string}")
    public void iPreviewTheMessage(String body) {
        preview(body, List.of());
    }

    private void preview(String body, List<Integer> listIds) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("body", body);
        req.put("list_ids", listIds);
        req.put("sample_size", 5);
        ResponseContext.set(ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(req)).post("/api/v1/admin/templates/preview"));
    }

    @Then("a preview sample should read {string}")
    public void aPreviewSampleShouldRead(String expected) {
        List<String> messages = ResponseContext.get().jsonPath().getList("samples.message");
        Assert.assertTrue("No sample reads '" + expected + "': " + messages, messages.contains(expected));
    }

    @Then("the preview should report unknown placeholder {string}")
    public void thePreviewShouldReportUnknownPlaceholder(String name) {
        List<String> unknown = ResponseContext.get().jsonPath().getList("unknown_placeholders");
        Assert.assertTrue(unknown + " lacks " + name, unknown.contains(name));
    }

    @Then("the preview template should be {string} with {int} page(s)")
    public void thePreviewTemplateShouldBe(String encoding, int pages) {
        Response resp = ResponseContext.get();
        Assert.assertEquals(resp.asString(), encoding, resp.jsonPath().getString("template.encoding"));
        Assert.assertEquals(resp.asString(), pages, resp.jsonPath().getInt("template.pages"));
    }
}
