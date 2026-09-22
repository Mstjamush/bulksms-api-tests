Feature: Admin senders (Client Administrator, scoped to their own client)

  @C3070
  Scenario: A client administrator registers a sender for their client
    Given I am authenticated as a client administrator for a new client
    And a new sender with a unique short code
    When I create the sender
    Then the response status should be 200
    And the JSON response should have a non-empty "short_code" field

  @C3071
  Scenario: Registering a sender with no short code is rejected
    Given I am authenticated as a client administrator for a new client
    And a new sender with no short code
    When I create the sender
    Then the response status should be 422

  @C3072
  Scenario: A user below Client Administrator cannot register a sender
    Given I am authenticated as a client administrator for a new client
    And a new user with role "Novice Account"
    When I create the user
    Then the response status should be 200
    Given I log in as the user created above
    And a new sender with a unique short code
    When I create the sender
    Then the response status should be 403
