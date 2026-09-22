package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import bulksmsapi.utils.RandomData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminBulkCampaignsSteps {

    private final Map<String, Object> body = new LinkedHashMap<>();

    @Given("a bulk campaign referencing that sender with recipients {string} and {string}")
    public void aBulkCampaignReferencingThatSenderWithRecipients(String recipient1, String recipient2) {
        body.clear();
        body.put("name", "QA Campaign " + RandomData.uniqueSuffix());
        body.put("message", "Hello from the automated suite");
        body.put("short_code", ClientAdminContext.lastSenderShortCode());
        body.put("recipients", List.of(recipient1, recipient2));
    }

    @Given("a bulk campaign referencing a sender that does not exist")
    public void aBulkCampaignReferencingASenderThatDoesNotExist() {
        body.clear();
        body.put("name", "QA Campaign " + RandomData.uniqueSuffix());
        body.put("message", "Hello from the automated suite");
        body.put("short_code", "NOSUCHSENDER" + RandomData.uniqueSuffix());
        body.put("recipients", List.of("254712345678"));
    }

    @When("I create the bulk campaign")
    public void iCreateTheBulkCampaign() {
        Response resp = ApiClient.adminAuthedSpec()
                .body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/bulk-campaigns/insert");
        ResponseContext.set(resp);
    }
}
