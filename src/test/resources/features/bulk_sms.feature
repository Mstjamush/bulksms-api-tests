Feature: POST /api/v1/sms/bulk - bulk SMS upload

  @C3020
  Scenario: Uploading a valid CSV is accepted for background processing
    Given a CSV file of valid recipients
    When I upload the bulk SMS file
    Then the response status should be 202
    And the JSON response should have a non-empty "uploadReference" field
    And the JSON response field "status" should equal "UPLOADED"

  @C3021
  Scenario: Rejects an unsupported file type
    Given an unsupported upload file
    When I upload the bulk SMS file
    Then the response status should be 400

  @C3022
  Scenario: Rejects a request with no file part
    When I upload the bulk SMS request with no file part
    Then the response status should be 422

  @C3023
  Scenario: Accepts a CSV with some blank phone numbers - invalid rows are dropped during background processing
    Given a CSV file with some blank phone numbers
    When I upload the bulk SMS file
    Then the response status should be 202

  @C3024
  Scenario: File extension matching is case-insensitive
    Given a CSV file of valid recipients with an uppercase extension
    When I upload the bulk SMS file
    Then the response status should be 202
