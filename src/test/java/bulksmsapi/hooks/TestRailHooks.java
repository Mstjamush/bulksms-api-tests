package bulksmsapi.hooks;

import bulksmsapi.config.Config;
import bulksmsapi.stepdefinitions.ClientAdminContext;
import bulksmsapi.stepdefinitions.ResponseContext;
import bulksmsapi.testrail.TestRailClient;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.jboss.logging.Logger;

/**
 * Reports every @C<id>-tagged scenario's outcome to TestRail after it runs.
 * Used by both entry points:
 *  - "mvn test": reports against the fixed testrail.run_id from testrail.properties.
 *  - TestRailDrivenRunner: reports against the run it just created, passed
 *    via the "testrail.run.id" system property (takes precedence).
 * Scenarios with no @C<id> tag are silently skipped - not every scenario has
 * to map to a TestRail case.
 */
public class TestRailHooks {

    private static final Logger LOG = Logger.getLogger(TestRailHooks.class);
    private static final TestRailClient testRail = new TestRailClient();

    private long startedAtMs;

    @Before
    public void beforeScenario(Scenario scenario) {
        ResponseContext.clear();
        ClientAdminContext.clear();
        startedAtMs = System.currentTimeMillis();
        LOG.infof("--- Starting: %s [%s] ---", scenario.getName(), String.join(" ", scenario.getSourceTagNames()));
    }

    @After
    public void afterScenario(Scenario scenario) {
        long elapsedSeconds = (System.currentTimeMillis() - startedAtMs) / 1000;
        LOG.infof("--- Finished: %s -> %s (%ds) ---", scenario.getName(), scenario.getStatus(), elapsedSeconds);

        if (Boolean.parseBoolean(System.getProperty("testrail.disabled", "false"))) return;

        int caseId = extractTestRailCaseId(scenario);
        if (caseId == -1) return;

        Integer runId = resolveRunId();
        if (runId == null) {
            LOG.warnf("TestRail: no run configured (testrail.run.id system property or "
                    + "testrail.run_id in testrail.properties) - skipping report for case %d", caseId);
            return;
        }

        int statusId = scenario.isFailed() ? 5 : 1; // 1 = Passed, 5 = Failed
        String comment = scenario.isFailed()
                ? scenario.getName() + " FAILED"
                : scenario.getName() + " PASSED";

        testRail.addResultForCase(runId, caseId, statusId, comment, elapsedSeconds);
    }

    private Integer resolveRunId() {
        String dynamic = System.getProperty("testrail.run.id");
        if (dynamic != null && !dynamic.isBlank()) return Integer.parseInt(dynamic.trim());

        String configured = Config.get("testrail.run_id");
        if (configured == null || configured.isBlank()) return null;
        return Integer.parseInt(configured.trim());
    }

    private int extractTestRailCaseId(Scenario scenario) {
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@C")) {
                try {
                    return Integer.parseInt(tag.substring(2));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }
}
