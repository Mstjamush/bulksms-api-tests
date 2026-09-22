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

    @When("I upload the bulk SMS file")
    public void iUploadTheBulkSmsFile() {
        Response resp = ApiClient.signedMultipartSpec()
                .multiPart("file", fileName, fileContent, mimeType)
                .post("/api/v1/sms/bulk");
        ResponseContext.set(resp);
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
