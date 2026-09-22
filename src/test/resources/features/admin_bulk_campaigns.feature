Feature: Admin bulk campaigns (Client Administrator, scoped to their own client)

  @C3080 @requires-rabbitmq
  Scenario: A client administrator creates a bulk campaign against their own sender
    Given I am authenticated as a client administrator for a new client
    And a new sender with a unique short code
    When I create the sender
    Then the response status should be 200
    Given a bulk campaign referencing that sender with recipients "254712345678" and "254798765432"
    When I create the bulk campaign
    Then the response status should be 200
    And the JSON response should have a non-empty "id" field

  @C3081
  Scenario: Referencing a sender the client does not own is rejected
    Given I am authenticated as a client administrator for a new client
    And a bulk campaign referencing a sender that does not exist
    When I create the bulk campaign
    Then the response status should be 400
