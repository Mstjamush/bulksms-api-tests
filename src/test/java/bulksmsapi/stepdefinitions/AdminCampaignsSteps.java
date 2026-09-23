package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import bulksmsapi.utils.RandomData;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;

/** Scheduled/personalised campaigns (POST /admin/campaigns and friends). */
public class AdminCampaignsSteps {

    private Map<String, Object> campaign(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "QA campaign " + RandomData.uniqueSuffix());
        body.put("sender_id", ClientAdminContext.lastSenderShortCode());
        body.put("message", message);
        body.put("list_ids", List.of(ScenarioData.<Integer>get("listId")));
        return body;
    }

    private void create(Map<String, Object> body) {
        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/campaigns");
        ResponseContext.set(resp);
        if (resp.statusCode() == 200) ScenarioData.put("campaignId", resp.jsonPath().getInt("id"));
    }

    @When("I send a campaign now to that broadcast list with message {string}")
    public void iSendACampaignNow(String message) {
        create(campaign(message));
    }

    @When("I schedule a campaign for that broadcast list {int} minutes from now with message {string}")
    public void iScheduleACampaign(int minutes, String message) {
        Map<String, Object> body = campaign(message);
        body.put("send_at", Instant.now().plus(minutes, ChronoUnit.MINUTES).toString());
        create(body);
    }

    @When("I schedule a campaign for that broadcast list in the past")
    public void iScheduleACampaignInThePast() {
        Map<String, Object> body = campaign("Too late");
        body.put("send_at", "2020-01-01T00:00:00Z");
        create(body);
    }

    @When("I estimate a campaign to that broadcast list with message {string}")
    public void iEstimateACampaign(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("list_ids", List.of(ScenarioData.<Integer>get("listId")));
        ResponseContext.set(ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/campaigns/estimate"));
    }

    @When("I send a campaign now to the numbers {string} with message {string}")
    public void iSendACampaignNowToTheNumbers(String numbers, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "QA campaign " + RandomData.uniqueSuffix());
        body.put("sender_id", ClientAdminContext.lastSenderShortCode());
        body.put("message", message);
        body.put("recipients", List.of(numbers.split(",\\s*")));
        create(body);
    }

    @When("I cancel that campaign")
    public void iCancelThatCampaign() {
        ResponseContext.set(ApiClient.adminAuthedSpec().post("/api/v1/admin/campaigns/" + ScenarioData.get("campaignId") + "/cancel"));
    }

    @When("I request that campaign")
    public void iRequestThatCampaign() {
        ResponseContext.set(ApiClient.adminAuthedSpec().get("/api/v1/admin/campaigns/" + ScenarioData.get("campaignId")));
    }

    @When("I request that campaign's messages with limit {int}")
    public void iRequestThatCampaignsMessages(int limit) {
        ResponseContext.set(ApiClient.adminAuthedSpec()
                .get("/api/v1/admin/campaigns/" + ScenarioData.get("campaignId") + "/messages?limit=" + limit));
    }

    @When("I export that campaign's messages as CSV")
    public void iExportThatCampaignsMessagesAsCsv() {
        ResponseContext.set(ApiClient.adminAuthedMultipartSpec()
                .get("/api/v1/admin/campaigns/" + ScenarioData.get("campaignId") + "/messages.csv"));
    }

    @When("I quick-send {string} as a bulk campaign with no message")
    public void iQuickSendAsABulkCampaignWithNoMessage(String file) {
        Response resp = ApiClient.adminAuthedMultipartSpec()
                .queryParam("name", "QA upload " + RandomData.uniqueSuffix())
                .queryParam("short_code", ClientAdminContext.lastSenderShortCode())
                .multiPart("file", file, TestFiles.read(file), TestFiles.mimeType(file))
                .post("/api/v1/admin/bulk-campaigns/upload");
        ResponseContext.set(resp);
        if (resp.statusCode() == 200) ScenarioData.put("campaignId", resp.jsonPath().getInt("id"));
    }

    @When("I request the campaigns list filtered by state {string}")
    public void iRequestTheCampaignsListFilteredByState(String state) {
        ResponseContext.set(ApiClient.adminAuthedSpec().get("/api/v1/admin/campaigns?state=" + state + "&limit=10"));
    }

    @Then("every message of that campaign should read one of {string} or {string}")
    public void everyMessageShouldReadOneOf(String a, String b) {
        Response resp = ApiClient.adminAuthedSpec()
                .get("/api/v1/admin/campaigns/" + ScenarioData.get("campaignId") + "/messages?limit=100");
        List<String> messages = resp.jsonPath().getList("message");
        Assert.assertFalse("No messages. Body: " + resp.asString(), messages.isEmpty());
        for (String m : messages) {
            Assert.assertTrue("Unexpected message '" + m + "'", m.equals(a) || m.equals(b));
        }
    }
}
