package bulksmsapi.testrail;

import bulksmsapi.config.Config;
import bulksmsapi.utils.HttpLogging;
import bulksmsapi.utils.JsonUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper over the TestRail v2 REST API (https://support.testrail.com/hc/en-us/sections/7076530574868-API-Reference).
 * Covers exactly what this suite's pipeline needs: read a project/suite's
 * case list, open a run against a chosen set of cases, and post one result
 * per case.
 */
public class TestRailClient {

    private static final Logger LOG = Logger.getLogger(TestRailClient.class);
    private static final int PAGE_SIZE = 250;

    private final String baseUrl;
    private final String username;
    private final String apiKey;

    public TestRailClient() {
        HttpLogging.install();
        this.baseUrl = Config.get("testrail.url") + "/index.php?/api/v2/";
        this.username = Config.get("testrail.username");
        this.apiKey = Config.get("testrail.api_key");
    }

    private RequestSpecification spec() {
        return RestAssured.given()
                .auth().preemptive().basic(username, apiKey)
                .baseUri(baseUrl)
                .contentType(ContentType.JSON);
    }

    /** Reads every case in the given project (and suite, for multi-suite-mode projects), paging through results. */
    @SuppressWarnings("unchecked")
    public List<TestRailCase> getCases(int projectId, Integer suiteId) {
        LOG.infof("Reading cases from TestRail (project %d%s)", projectId, suiteId != null ? ", suite " + suiteId : "");
        List<TestRailCase> all = new ArrayList<>();
        int offset = 0;

        while (true) {
            StringBuilder path = new StringBuilder("get_cases/").append(projectId)
                    .append("&offset=").append(offset)
                    .append("&limit=").append(PAGE_SIZE);
            if (suiteId != null) path.append("&suite_id=").append(suiteId);

            Response resp = spec().get(path.toString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("TestRail get_cases failed (" + resp.statusCode() + "): " + resp.asString());
            }

            List<Map<String, Object>> page = resp.jsonPath().get("cases") != null
                    ? resp.jsonPath().getList("cases")
                    : resp.jsonPath().getList("$");

            for (Map<String, Object> c : page) {
                all.add(new TestRailCase(((Number) c.get("id")).intValue(), String.valueOf(c.get("title"))));
            }

            if (page.size() < PAGE_SIZE) break;
            offset += PAGE_SIZE;
        }

        LOG.infof("Read %d case(s) from TestRail", all.size());
        return all;
    }

    /** Creates a new run containing exactly the given cases; returns its run id. */
    public int addRun(int projectId, Integer suiteId, String name, List<Integer> caseIds) {
        StringBuilder body = new StringBuilder("{");
        body.append("\"name\":\"").append(JsonUtil.escape(name)).append("\",");
        body.append("\"include_all\":false,");
        if (suiteId != null) body.append("\"suite_id\":").append(suiteId).append(",");
        body.append("\"case_ids\":[");
        for (int i = 0; i < caseIds.size(); i++) {
            if (i > 0) body.append(",");
            body.append(caseIds.get(i));
        }
        body.append("]}");

        Response resp = spec().body(body.toString()).post("add_run/" + projectId);
        if (resp.statusCode() != 200) {
            throw new RuntimeException("TestRail add_run failed (" + resp.statusCode() + "): " + resp.asString());
        }
        int runId = resp.jsonPath().getInt("id");
        LOG.infof("Created TestRail run #%d (%s) with %d case(s)", runId, name, caseIds.size());
        return runId;
    }

    /** statusId: 1 = Passed, 2 = Blocked, 4 = Retest, 5 = Failed (TestRail's default status ids). */
    public void addResultForCase(int runId, int caseId, int statusId, String comment, long elapsedSeconds) {
        String body = "{\"status_id\":" + statusId
                + ",\"comment\":\"" + JsonUtil.escape(comment) + "\""
                + ",\"elapsed\":\"" + Math.max(1, elapsedSeconds) + "s\"}";

        Response resp = spec().body(body).post("add_result_for_case/" + runId + "/" + caseId);
        if (resp.statusCode() == 200) {
            LOG.infof("Reported case %d (status %d) to TestRail run #%d", caseId, statusId, runId);
        } else {
            LOG.errorf("TestRail add_result_for_case failed for case %d (%d): %s",
                    caseId, resp.statusCode(), resp.asString());
        }
    }
}
