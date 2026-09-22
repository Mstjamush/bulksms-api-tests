Feature: Admin users

  @C3060
  Scenario: A Super Administrator creates a user for a client
    Given I am authenticated as the super admin
    And a new client with a unique name
    When I create the client
    Then the response status should be 200
    Given a new user for that client with role "Digital Administrator"
    When I create the user
    Then the response status should be 200
    And the JSON response should have a non-empty "email_address" field

  @C3061
  Scenario: A Super Administrator must specify a client_id
    Given I am authenticated as the super admin
    And a new user with no client_id
    When I create the user
    Then the response status should be 400

  @C3062
  Scenario: A password shorter than 6 characters is rejected
    Given I am authenticated as the super admin
    And a new user with password "abc12"
    When I create the user
    Then the response status should be 422

  @C3063
  Scenario: A client administrator creates a user under their own client
    Given I am authenticated as a client administrator for a new client
    And a new user with role "Sales Agent"
    When I create the user
    Then the response status should be 200

  @C3064
  Scenario: A client administrator cannot assign the Client Administrator role
    Given I am authenticated as a client administrator for a new client
    And a new user with role "Client Administrator"
    When I create the user
    Then the response status should be 403
