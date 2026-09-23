Feature: POST /api/v1/sms as a known, billed client (X-Api-Client)

  Background:
    Given I am authenticated as a client administrator for a new client
    And a sender has been provisioned for my client
    And I know my API client id

  @C3130
  Scenario: A client-identified send is accepted and charged pages x rate
    When I send a billed SMS to "254712345678" with message "Your OTP is 4821" from my sender
    Then the response status should be 202
    And the JSON response should have a non-empty "reference" field
    When I request my ledger filtered by "DEBIT"
    Then the JSON response field "[0].units" should be the number -1
    And the JSON response field "[0].amount" should be the number -1.0

  @C3131
  Scenario: A known client must name one of its own senders
    When I send a billed SMS to "254712345678" with message "Hello" and no sender
    Then the response status should be 400

  @C3132
  Scenario: A known client cannot send under another client's sender
    When I send a billed SMS to "254712345678" with message "Hello" from sender "MYAPP"
    Then the response status should be 400

  @C3133
  Scenario: An unknown API client is refused
    When I send an SMS as unknown API client 999999999
    Then the response status should be 401

  @C3134
  Scenario: The number may be sent as "phone_number" as well as "to"
    When I send a signed SMS using the "phone_number" field for the number "0712345678"
    Then the response status should be 202

  @C3135
  Scenario: An invalid phone number is rejected before anything is stored
    When I send a billed SMS to "12" with message "Hello" from my sender
    Then the response status should be 422

  @C3136
  Scenario: A client without credit is refused with 402
    Given I am authenticated as a client administrator for a new client with no credit
    And a sender has been provisioned for my client
    And I know my API client id
    When I send a billed SMS to "254712345678" with message "Hello" from my sender
    Then the response status should be 402
