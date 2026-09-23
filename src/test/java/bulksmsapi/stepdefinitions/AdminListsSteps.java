package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.AdminFixtures;
import bulksmsapi.utils.AdminRoles;
import bulksmsapi.utils.AdminSession;
import bulksmsapi.utils.ApiClient;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;

/** Broadcast lists: uploads (CSV/XLSX/XLS), duplicate and invalid-row reporting, import history. */
public class AdminListsSteps {

    @When("I upload {string} as a new broadcast list")
    public void iUploadAsANewBroadcastList(String file) {
        Response resp = ApiClient.adminAuthedMultipartSpec()
                .multiPart("name", "QA list " + bulksmsapi.utils.RandomData.uniqueSuffix())
                .multiPart("file", file, TestFiles.read(file), TestFiles.mimeType(file))
                .post("/api/v1/admin/lists");
        ResponseContext.set(resp);
        if (resp.statusCode() == 200) {
            ScenarioData.put("listId", resp.jsonPath().getInt("list.id"));
        }
    }

    @When("I append {string} to that broadcast list")
    public void iAppendToThatBroadcastList(String file) {
        Response resp = ApiClient.adminAuthedMultipartSpec()
                .multiPart("file", file, TestFiles.read(file), TestFiles.mimeType(file))
                .post("/api/v1/admin/lists/" + ScenarioData.get("listId") + "/upload");
        ResponseContext.set(resp);
    }

    @Then("the import should report {int} added, {int} duplicate(s) and {int} invalid")
    public void theImportShouldReport(int added, int duplicates, int invalid) {
        Response resp = ResponseContext.get();
        Assert.assertEquals("added. Body: " + resp.asString(), added, resp.jsonPath().getInt("import.added"));
        Assert.assertEquals("duplicates. Body: " + resp.asString(), duplicates, resp.jsonPath().getInt("import.duplicates"));
        Assert.assertEquals("invalid. Body: " + resp.asString(), invalid, resp.jsonPath().getInt("import.invalid"));
    }

    @Then("the import should report duplicate {string} because {string}")
    public void theImportShouldReportDuplicate(String number, String reason) {
        Response resp = ResponseContext.get();
        List<Map<String, Object>> samples = resp.jsonPath().getList("import.duplicate_samples");
        Assert.assertTrue("No duplicate " + number + " (" + reason + ") in " + samples, samples.stream()
                .anyMatch(s -> number.equals(String.valueOf(s.get("value"))) && reason.equals(s.get("reason"))));
    }

    @Then("the broadcast list should have placeholders {string}")
    public void theBroadcastListShouldHavePlaceholders(String expected) {
        List<String> actual = ResponseContext.get().jsonPath().getList("list.variables");
        Assert.assertEquals(Arrays.asList(expected.split(",\\s*")), actual);
    }

    @When("I request the import history of that broadcast list")
    public void iRequestTheImportHistory() {
        Response resp = ApiClient.adminAuthedSpec().get("/api/v1/admin/lists/" + ScenarioData.get("listId") + "/imports");
        ResponseContext.set(resp);
        if (resp.statusCode() == 200 && !resp.jsonPath().getList("$").isEmpty()) {
            ScenarioData.put("importId", resp.jsonPath().getInt("[0].id"));
        }
    }

    @When("I download the skipped rows of the latest import")
    public void iDownloadTheSkippedRowsOfTheLatestImport() {
        ResponseContext.set(ApiClient.adminAuthedMultipartSpec().get("/api/v1/admin/lists/" + ScenarioData.get("listId")
                + "/imports/" + ScenarioData.get("importId") + "/issues.csv"));
    }

    @When("I request the members of that broadcast list with limit {int}")
    public void iRequestTheMembers(int limit) {
        ResponseContext.set(ApiClient.adminAuthedSpec()
                .get("/api/v1/admin/lists/" + ScenarioData.get("listId") + "/members?limit=" + limit));
    }

    @When("another client's administrator requests that broadcast list")
    public void anotherClientsAdministratorRequestsThatBroadcastList() {
        int listId = ScenarioData.get("listId");
        AdminSession.loginAsSuperAdmin();
        AdminFixtures.ProvisionedClient other = AdminFixtures.createClient();
        AdminFixtures.ProvisionedUser otherAdmin = AdminFixtures.createUser(other.clientId(), AdminRoles.CLIENT_ADMIN);
        AdminSession.login(otherAdmin.email(), otherAdmin.password());
        ResponseContext.set(ApiClient.adminAuthedSpec().get("/api/v1/admin/lists/" + listId));
    }
}
