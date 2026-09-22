Feature: Admin clients (Super Administrator only)

  Background:
    Given I am authenticated as the super admin

  @C3050
  Scenario: Creating a client and finding it in the client list
    Given a new client with a unique name
    When I create the client
    Then the response status should be 200
    When I list clients
    Then the response status should be 200
    And the client list should include the client created earlier in this scenario

  @C3051
  Scenario: Creating a client with no client_name is rejected
    Given a new client with no client_name
    When I create the client
    Then the response status should be 422

  @C3052
  Scenario: Creating a client with an invalid email address is rejected
    Given a new client with an invalid email address
    When I create the client
    Then the response status should be 422

  @C3053
  Scenario: A client administrator cannot list clients
    Given I am authenticated as a client administrator for a new client
    When I list clients
    Then the response status should be 403

  @C3054
  Scenario: A client administrator cannot create a client
    Given I am authenticated as a client administrator for a new client
    And a new client with a unique name
    When I create the client
    Then the response status should be 403
