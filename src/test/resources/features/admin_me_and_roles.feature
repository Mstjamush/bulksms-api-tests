Feature: Admin session endpoints (/me, /roles, /sender-id-types)

  Background:
    Given I am authenticated as the super admin

  @C3040
  Scenario: Fetching my own profile
    When I GET "/api/v1/admin/me"
    Then the response status should be 200
    And the JSON response should have a non-empty "email" field

  @C3041
  Scenario: Profile endpoint requires a token
    When I request "/api/v1/admin/me" without a token
    Then the response status should be 401

  @C3042
  Scenario: Listing available roles
    When I GET "/api/v1/admin/roles"
    Then the response status should be 200

  @C3043
  Scenario: Listing sender ID types
    When I GET "/api/v1/admin/sender-id-types"
    Then the response status should be 200

  @C3044
  Scenario: Profile endpoint rejects a malformed authorization header
    When I request "/api/v1/admin/me" with a malformed authorization header
    Then the response status should be 401

  @C3045
  Scenario: Profile endpoint rejects an invalid token
    When I request "/api/v1/admin/me" with an invalid token
    Then the response status should be 401
