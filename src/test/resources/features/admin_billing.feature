Feature: KES billing - wallet, plans, bundles and the ledger

  Background:
    Given I am authenticated as a client administrator for a new client

  @C3120
  Scenario: A top-up with a payment reference is credited once, even if submitted twice
    When the super admin tops up my client with KES "500" using a new payment reference
    Then the response status should be 200
    And the JSON response field "summary.wallet.balance" should be the number 1500
    When the super admin submits that top-up again
    Then the JSON response field "summary.wallet.balance" should be the number 1500

  @C3121
  Scenario: A client administrator cannot top up their own wallet
    When I try to top up my own wallet with KES "1000000"
    Then the response status should be 403

  @C3122
  Scenario: Subscribing to a plan charges its fee and grants its SMS allowance at the plan rate
    Given the super admin has created a plan at KES "0.80" per SMS with 100 SMS for a KES "200" fee
    When the super admin subscribes my client to that plan
    Then the response status should be 200
    And the JSON response field "wallet.balance" should be the number 800
    And the JSON response field "wallet.rate_per_sms" should be the number 0.8
    And the JSON response field "subscription.allowance_remaining" should be the number 100

  @C3123
  Scenario: A bundle converts KES into whole SMS at its negotiated rate
    When the super admin adds a KES "50" bundle at KES "0.35" per SMS valid for 30 days
    Then the response status should be 200
    And the JSON response field "bundle.units" should be the number 142
    And the JSON response field "bundle.wallet_remainder" should be the number 0.3
    And the JSON response field "summary.bundle_units" should be the number 142

  @C3124
  Scenario: A requested bundle becomes spendable once the super admin approves it
    When I request a bundle of KES "100"
    Then the response status should be 200
    And the JSON response field "status" should equal "PENDING"
    When the super admin approves that bundle request at KES "0.50" per SMS for 60 days
    Then the response status should be 200
    And the JSON response field "request.status" should equal "APPROVED"
    And the JSON response field "bundle.units" should be the number 200

  @C3125
  Scenario: A rejected bundle request cannot be approved afterwards
    When I request a bundle of KES "100"
    And the super admin rejects that bundle request
    Then the response status should be 200
    When the super admin approves that bundle request at KES "0.50" per SMS for 60 days
    Then the response status should be 400

  @C3126
  Scenario: Sends are paid from the soonest-expiring bundle before the wallet
    Given a sender has been provisioned for my client
    When the super admin adds a KES "10" bundle at KES "0.50" per SMS valid for 7 days
    And I upload "personalised_list.csv" as a new broadcast list
    And I send a campaign now to that broadcast list with message "Hi {{name}}"
    And I request my billing summary
    Then the JSON response field "bundle_units" should be the number 17
    And the JSON response field "wallet.balance" should be the number 1000

  @C3127
  Scenario: Every movement is in the ledger
    Given a sender has been provisioned for my client
    When I upload "personalised_list.csv" as a new broadcast list
    And I send a campaign now to that broadcast list with message "Hi {{name}}"
    And I request my ledger filtered by "DEBIT"
    Then the response status should be 200
    And the JSON response should be a list of at least 1 item
    And the JSON response field "[0].units" should be the number -3
    And the JSON response field "[0].amount" should be the number -3.0
