package bulksmsapi.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Plain "run everything not ignored" entry point (mvn test). The tag filter
 * here can be overridden at runtime via -Dcucumber.filter.tags, which is
 * exactly what TestRailDrivenRunner does to run only the scenarios matching
 * cases it just fetched from TestRail.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"bulksmsapi.stepdefinitions", "bulksmsapi.hooks"},
        plugin = {"pretty", "html:target/cucumber-html-report", "json:target/cucumber.json"},
        monochrome = true,
        tags = "not @ignore"
)
public class TestRunner {
}
