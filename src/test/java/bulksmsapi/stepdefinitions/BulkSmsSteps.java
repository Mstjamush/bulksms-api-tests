package bulksmsapi.stepdefinitions;

import bulksmsapi.utils.ApiClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class BulkSmsSteps {

    private byte[] fileContent;
    private String fileName;
    private String mimeType;
    private String message;

    @Given("a CSV file of valid recipients")
    public void aCsvFileOfValidRecipients() {
        fileContent = readClasspathResource("testdata/valid_recipients.csv");
        fileName = "valid_recipients.csv";
        mimeType = "text/csv";
    }

    @Given("an unsupported upload file")
    public void anUnsupportedUploadFile() {
        fileContent = readClasspathResource("testdata/unsupported_upload.txt");
        fileName = "unsupported_upload.txt";
        mimeType = "text/plain";
    }

    @Given("a CSV file with some blank phone numbers")
    public void aCsvFileWithSomeBlankPhoneNumbers() {
        fileContent = readClasspathResource("testdata/partial_valid_recipients.csv");
        fileName = "partial_valid_recipients.csv";
        mimeType = "text/csv";
    }

    @Given("a CSV file of valid recipients with an uppercase extension")
    public void aCsvFileOfValidRecipientsWithAnUppercaseExtension() {
        // Same bytes as the plain valid-recipients fixture - only the
        // filename's case differs, to exercise parsing.py's filename.lower()
        // extension dispatch.
        fileContent = readClasspathResource("testdata/valid_recipients.csv");
        fileName = "VALID_RECIPIENTS.CSV";
        mimeType = "text/csv";
    }

    @Given("the bulk file {string} with message {string}")
    public void theBulkFileWithMessage(String file, String text) {
        fileContent = TestFiles.read(file);
        fileName = file;
        mimeType = TestFiles.mimeType(file);
        message = text;
    }

    @Given("the bulk file {string} with no message")
    public void theBulkFileWithNoMessage(String file) {
        theBulkFileWithMessage(file, null);
    }

    @When("I upload the bulk SMS file")
    public void iUploadTheBulkSmsFile() {
        io.restassured.specification.RequestSpecification spec = ApiClient.signedMultipartSpec()
                .multiPart("file", fileName, fileContent, mimeType);
        if (message != null) spec = spec.multiPart("message", message);
        Response resp = spec.post("/api/v1/sms/bulk");
        ResponseContext.set(resp);
        if (resp.statusCode() == 202) ScenarioData.put("uploadReference", resp.jsonPath().getString("uploadReference"));
    }

    /** Processing happens in the background - poll the admin uploads report for the outcome. */
    @io.cucumber.java.en.Then("the upload should finish as {string} with {int} valid record(s)")
    public void theUploadShouldFinishAs(String status, int valid) throws InterruptedException {
        String reference = ScenarioData.get("uploadReference");
        java.util.Map<String, Object> upload = null;
        for (int i = 0; i < 40 && (upload == null || "UPLOADED".equals(upload.get("status"))
                || "PROCESSING".equals(upload.get("status"))); i++) {
            Thread.sleep(250);
            java.util.List<java.util.Map<String, Object>> rows = ApiClient.superAdminSpec()
                    .get("/api/v1/admin/uploads?limit=50").jsonPath().getList("$");
            upload = rows.stream().filter(r -> reference.equals(r.get("upload_reference"))).findFirst().orElse(null);
        }
        org.junit.Assert.assertNotNull("Upload " + reference + " not in the uploads report", upload);
        org.junit.Assert.assertEquals("status of " + upload, status, upload.get("status"));
        org.junit.Assert.assertEquals("valid_records of " + upload, valid, ((Number) upload.get("valid_records")).intValue());
    }

    @When("I upload the bulk SMS request with no file part")
    public void iUploadTheBulkSmsRequestWithNoFilePart() {
        Response resp = ApiClient.signedMultipartSpec().post("/api/v1/sms/bulk");
        ResponseContext.set(resp);
    }

    private byte[] readClasspathResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("Missing test resource: " + path);
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
