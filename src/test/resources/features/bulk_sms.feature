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

  @C3025
  Scenario: A personalised file is sent using a message template over its columns
    Given the bulk file "personalised_list.csv" with message "Hi {{name}}, please pay KES {{amount|the balance}}"
    When I upload the bulk SMS file
    Then the response status should be 202
    And the upload should finish as "COMPLETED" with 3 valid records

  @C3026
  Scenario: A numbers-only file sends the same message to every number
    Given the bulk file "numbers_only.csv" with message "Offices closed on Monday"
    When I upload the bulk SMS file
    Then the response status should be 202
    And the upload should finish as "COMPLETED" with 2 valid records

  @C3027
  Scenario: A file with a message column sends each row's own text; empty messages are invalid
    Given the bulk file "per_row_messages.csv" with no message
    When I upload the bulk SMS file
    Then the response status should be 202
    And the upload should finish as "COMPLETED" with 2 valid records

  @C3028
  Scenario: A numbers-only file with no message fails with a clear reason
    Given the bulk file "numbers_only.csv" with no message
    When I upload the bulk SMS file
    Then the response status should be 202
    And the upload should finish as "FAILED" with 0 valid records

  @C3029
  Scenario: A legacy Excel .xls file is accepted
    Given the bulk file "loans.xls" with message "Hi {{name}}, KES {{amount}} is due"
    When I upload the bulk SMS file
    Then the response status should be 202
    And the upload should finish as "COMPLETED" with 2 valid records
