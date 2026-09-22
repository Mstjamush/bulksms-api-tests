package bulksmsapi.testrail;

import bulksmsapi.config.Config;
import bulksmsapi.runners.TestRunner;
import org.jboss.logging.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The end-to-end TestRail pipeline requested for this suite:
 *   1. READ  - pulls the case list for testrail.project_id (+ testrail.suite_id)
 *              from TestRail (TestRailClient.getCases already logs this step).
 *   2. RUN   - opens a fresh TestRail run containing exactly those cases, then
 *              runs only the Cucumber scenarios tagged @C<id> for a case in
 *              that list (via TestRunner, so it's the exact same suite
 *              "mvn test" runs - just filtered and pointed at this run).
 *   3. UPDATE - each scenario reports its own result as it finishes
 *              (TestRailHooks), against the run created in step 2.
 *
 * A scenario's @C<id> tag only matters if that id is an actual case in your
 * TestRail project/suite - see the README for how to align feature file tags
 * with real case ids. Everything logged here also goes to
 * target/logs/bulksms-api-tests.log (see logback-test.xml).
 *
 * Usage:
 *   mvn test-compile exec:java -Dexec.mainClass=bulksmsapi.testrail.TestRailDrivenRunner
 *   mvn test-compile exec:java -Dexec.mainClass=bulksmsapi.testrail.TestRailDrivenRunner -Dexec.args="--dry-run"
 */
public class TestRailDrivenRunner {

    private static final Logger LOG = Logger.getLogger(TestRailDrivenRunner.class);

    public static void main(String[] args) {
        boolean dryRun = Arrays.asList(args).contains("--dry-run");

        int projectId = Config.getInt("testrail.project_id");
        String suiteIdRaw = Config.get("testrail.suite_id");
        Integer suiteId = (suiteIdRaw == null || suiteIdRaw.isBlank()) ? null : Integer.parseInt(suiteIdRaw.trim());

        TestRailClient client = new TestRailClient();

        List<TestRailCase> cases = client.getCases(projectId, suiteId);
        if (cases.isEmpty()) {
            LOG.error("No cases found for this project/suite - nothing to run. Check "
                    + "testrail.project_id/testrail.suite_id in testrail.properties.");
            System.exit(1);
        }

        List<Integer> caseIds = cases.stream().map(TestRailCase::id).collect(Collectors.toList());
        String tagExpression = caseIds.stream().map(id -> "@C" + id).collect(Collectors.joining(" or "));

        for (TestRailCase c : cases) {
            LOG.infof("  C%d - %s", c.id(), c.title());
        }

        if (dryRun) {
            LOG.info("--dry-run: not creating a run or executing anything.");
            LOG.infof("Tag filter that would be used: %s", tagExpression);
            LOG.info("Tip: `grep -rlo '@C[0-9]*' src/test/resources/features` shows which of the above ids "
                    + "your feature files actually cover today.");
            return;
        }

        String runName = "Automated run " + LocalDateTime.now();
        int runId = client.addRun(projectId, suiteId, runName, caseIds);

        System.setProperty("cucumber.filter.tags", tagExpression);
        System.setProperty("testrail.run.id", String.valueOf(runId));

        Result result = JUnitCore.runClasses(TestRunner.class);

        LOG.infof("Ran %d scenario(s), %d failed.", result.getRunCount(), result.getFailureCount());
        for (Failure failure : result.getFailures()) {
            LOG.infof(" - %s", failure.getTestHeader());
        }
        LOG.infof("Results pushed to TestRail: %s/index.php?/runs/view/%d", Config.get("testrail.url"), runId);

        if (result.getRunCount() == 0) {
            LOG.warn("0 scenarios matched. None of this suite's feature files currently have a @C<id> tag for "
                    + "any case id fetched above - see the README's \"Mapping scenarios to TestRail cases\" section.");
        }

        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
