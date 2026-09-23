Feature: Reports and paginated admin lists

  Background:
    Given I am authenticated as a client administrator for a new client

  @C3140
  Scenario: The overview report includes the client's billing summary
    When I GET "/api/v1/admin/reports/overview?days=7"
    Then the response status should be 200
    And the JSON response should have a non-empty "messages" field
    And the JSON response field "billing.wallet.balance" should be the number 1000

  @C3141
  Scenario: The daily report has one point per day
    When I GET "/api/v1/admin/reports/daily?days=7"
    Then the response status should be 200
    And the JSON response should be a list of exactly 7 items

  @C3142
  Scenario: The daily report can be exported as CSV
    When I GET "/api/v1/admin/reports/daily.csv?days=3"
    Then the response status should be 200
    And the response body should contain "date,messages"

  @C3143
  Scenario: List endpoints return a total count header
    When I GET "/api/v1/admin/campaigns?limit=5"
    Then the response status should be 200
    And the response header "X-Total-Count" should be a number of at least 0

  @C3144
  Scenario: The current user can be read
    When I GET "/api/v1/admin/me"
    Then the response status should be 200
    And the JSON response should have a non-empty "client_id" field
