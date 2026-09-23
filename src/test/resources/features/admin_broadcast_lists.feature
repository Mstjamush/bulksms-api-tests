Feature: Broadcast lists - upload, duplicates reporting, import history

  Background:
    Given I am authenticated as a client administrator for a new client

  @C3090
  Scenario: Uploading a personalised CSV makes its other columns placeholders
    When I upload "personalised_list.csv" as a new broadcast list
    Then the response status should be 200
    And the import should report 3 added, 0 duplicates and 0 invalid
    And the broadcast list should have placeholders "name, amount"
    And the JSON response field "import.phone_column" should equal "phone_number"

  @C3091
  Scenario: A number repeated in the file is added once and reported as a duplicate
    When I upload "duplicates_list.csv" as a new broadcast list
    Then the response status should be 200
    And the import should report 2 added, 2 duplicates and 1 invalid
    And the import should report duplicate "254712300101" because "repeated in this file"
    And the import should report duplicate "254712300102" because "repeated in this file"

  @C3092
  Scenario: Appending a number already on the list reports it as a duplicate
    When I upload "duplicates_list.csv" as a new broadcast list
    And I append "duplicates_append.csv" to that broadcast list
    Then the response status should be 200
    And the import should report 1 added, 1 duplicate and 0 invalid
    And the import should report duplicate "254712300102" because "already on this list"

  @C3093
  Scenario: Every import is kept in the list's history with its skipped rows downloadable
    When I upload "duplicates_list.csv" as a new broadcast list
    And I append "duplicates_append.csv" to that broadcast list
    And I request the import history of that broadcast list
    Then the response status should be 200
    And the JSON response should be a list of exactly 2 items
    When I download the skipped rows of the latest import
    Then the response status should be 200
    And the response body should contain "254712300102,duplicate,already on this list"

  @C3094
  Scenario: A legacy Excel .xls file can be uploaded as a broadcast list
    When I upload "loans.xls" as a new broadcast list
    Then the response status should be 200
    And the import should report 2 added, 0 duplicates and 0 invalid
    And the broadcast list should have placeholders "name, amount"

  @C3095
  Scenario: A file with no phone number column is rejected
    When I upload "no_phone_column.csv" as a new broadcast list
    Then the response status should be 400

  @C3096
  Scenario: An unsupported file type is rejected
    When I upload "unsupported_upload.txt" as a new broadcast list
    Then the response status should be 400

  @C3097
  Scenario: List members are paginated with a total count header
    When I upload "personalised_list.csv" as a new broadcast list
    And I request the members of that broadcast list with limit 2
    Then the response status should be 200
    And the JSON response should be a list of exactly 2 items
    And the response header "X-Total-Count" should equal "3"

  @C3098
  Scenario: Another client cannot see my broadcast list
    When I upload "personalised_list.csv" as a new broadcast list
    And another client's administrator requests that broadcast list
    Then the response status should be 404
