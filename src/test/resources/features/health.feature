Feature: Health check

  @C3001
  Scenario: Health endpoint reports its dependency checks
    When I call the health endpoint
    Then the response status should be 200 or 503
    And the JSON response should have a non-empty "status" field
    And the JSON response should have a non-empty "checks" field
