package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import bulksmsapi.utils.JsonUtil;
import bulksmsapi.utils.RandomData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;

/** KES billing: wallet top-ups, plans/subscriptions, bundles, bundle requests, ledger. */
public class AdminBillingSteps {

    private int clientId() {
        return ClientAdminContext.client().clientId();
    }

    @When("the super admin tops up my client with KES {string} using a new payment reference")
    public void theSuperAdminTopsUp(String amount) {
        ScenarioData.put("paymentRef", "QA-PAY-" + RandomData.uniqueSuffix());
        topUp(amount);
    }

    @When("the super admin submits that top-up again")
    public void theSuperAdminSubmitsThatTopUpAgain() {
        topUp(ScenarioData.get("topupAmount"));
    }

    private void topUp(String amount) {
        ScenarioData.put("topupAmount", amount);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        body.put("reference", ScenarioData.get("paymentRef"));
        ResponseContext.set(ApiClient.superAdminSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/billing/clients/" + clientId() + "/topup"));
    }

    @When("I try to top up my own wallet with KES {string}")
    public void iTryToTopUpMyOwnWallet(String amount) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        ResponseContext.set(ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/billing/clients/" + clientId() + "/topup"));
    }

    @When("I request my billing summary")
    public void iRequestMyBillingSummary() {
        ResponseContext.set(ApiClient.adminAuthedSpec().get("/api/v1/admin/billing/summary"));
    }

    @Given("the super admin has created a plan at KES {string} per SMS with {int} SMS for a KES {string} fee")
    public void theSuperAdminHasCreatedAPlan(String rate, int allowance, String fee) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "QA plan " + RandomData.uniqueSuffix());
        body.put("rate_per_sms", rate);
        body.put("allowance_units", allowance);
        body.put("fee", fee);
        body.put("period_days", 30);
        Response resp = ApiClient.superAdminSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/plans");
        Assert.assertEquals("Create plan: " + resp.asString(), 200, resp.statusCode());
        ScenarioData.put("planId", resp.jsonPath().getInt("id"));
    }

    @When("the super admin subscribes my client to that plan")
    public void theSuperAdminSubscribesMyClientToThatPlan() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("plan_id", ScenarioData.<Integer>get("planId"));
        ResponseContext.set(ApiClient.superAdminSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/billing/clients/" + clientId() + "/subscriptions"));
    }

    @When("the super admin adds a KES {string} bundle at KES {string} per SMS valid for {int} days")
    public void theSuperAdminAddsABundle(String amount, String rate, int days) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        body.put("rate_per_sms", rate);
        body.put("validity_days", days);
        ResponseContext.set(ApiClient.superAdminSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/billing/clients/" + clientId() + "/bundles"));
    }

    @When("I request a bundle of KES {string}")
    public void iRequestABundle(String amount) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        body.put("note", "Automated suite request");
        Response resp = ApiClient.adminAuthedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/bundle-requests");
        ResponseContext.set(resp);
        if (resp.statusCode() == 200) ScenarioData.put("bundleRequestId", resp.jsonPath().getInt("id"));
    }

    @When("the super admin approves that bundle request at KES {string} per SMS for {int} days")
    public void theSuperAdminApproves(String rate, int days) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("rate_per_sms", rate);
        body.put("validity_days", days);
        ResponseContext.set(ApiClient.superAdminSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/bundle-requests/" + ScenarioData.get("bundleRequestId") + "/approve"));
    }

    @When("the super admin rejects that bundle request")
    public void theSuperAdminRejects() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("note", "Not this month");
        ResponseContext.set(ApiClient.superAdminSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/bundle-requests/" + ScenarioData.get("bundleRequestId") + "/reject"));
    }

    @When("I request my ledger filtered by {string}")
    public void iRequestMyLedgerFilteredBy(String txnType) {
        ResponseContext.set(ApiClient.adminAuthedSpec().get("/api/v1/admin/billing/ledger?limit=10&txn_type=" + txnType));
    }
}
