package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import bulksmsapi.utils.RandomData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdminSendersSteps {

    private final Map<String, Object> body = new LinkedHashMap<>();
    private String shortCode;

    @Given("a new sender with a unique short code")
    public void aNewSenderWithAUniqueShortCode() {
        shortCode = "QA" + RandomData.uniqueSuffix();
        body.clear();
        body.put("short_code", shortCode);
        body.put("send_type_id", 1); // Transactional - seeded by seed_admin.py
        body.put("keywords", "QA");
    }

    @Given("a new sender with no short code")
    public void aNewSenderWithNoShortCode() {
        shortCode = null;
        body.clear();
        body.put("send_type_id", 1);
        body.put("keywords", "QA");
    }

    @When("I create the sender")
    public void iCreateTheSender() {
        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/senders");
        ResponseContext.set(resp);

        if (resp.statusCode() == 200 && shortCode != null) {
            ClientAdminContext.setLastSenderShortCode(shortCode);
        }
    }
}
