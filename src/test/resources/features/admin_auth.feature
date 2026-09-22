Feature: POST /api/v1/admin/auth/login

  @C3030
  Scenario: Logging in with the correct credentials succeeds
    Given the configured super admin credentials
    When I log in to the admin API
    Then the response status should be 200
    And the JSON response should have a non-empty "token" field

  @C3031
  Scenario: Logging in with the wrong password is rejected
    Given the configured super admin email with password "definitely-wrong-password"
    When I log in to the admin API
    Then the response status should be 401

  @C3032
  Scenario: Logging in with a missing password is rejected
    Given admin login credentials "someone@example.test" and ""
    When I log in to the admin API
    Then the response status should be 422

  @C3033
  Scenario: Logging in with an email that has no account is rejected
    Given admin login credentials "no.such.admin.user@example.com" and "whatever-password"
    When I log in to the admin API
    Then the response status should be 401

  @C3034
  Scenario: Logging in with a missing email field is rejected
    Given admin login credentials with no email and password "whatever-password"
    When I log in to the admin API
    Then the response status should be 422
