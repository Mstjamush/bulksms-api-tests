Feature: Scheduled and personalised campaigns

  Background:
    Given I am authenticated as a client administrator for a new client
    And a sender has been provisioned for my client

  @C3110
  Scenario: A send-now campaign to a personalised list is dispatched with each recipient's values
    When I upload "personalised_list.csv" as a new broadcast list
    And I send a campaign now to that broadcast list with message "Hi {{name}}, KES {{amount|0}} due"
    Then the response status should be 200
    And the JSON response field "state" should equal "DISPATCHED"
    And the JSON response field "recipient_count" should be the number 3
    And the JSON response field "total_pages" should be the number 3

  @C3111
  Scenario: The cost estimate matches pages x rate before sending
    When I upload "personalised_list.csv" as a new broadcast list
    And I estimate a campaign to that broadcast list with message "Hello {{name}}"
    Then the response status should be 200
    And the JSON response field "recipients" should be the number 3
    And the JSON response field "pages" should be the number 3
    And the JSON response field "cost" should be the number 3.0
    And the JSON response field "sufficient" should equal "true"

  @C3112
  Scenario: A campaign scheduled for later waits as SCHEDULED
    When I upload "personalised_list.csv" as a new broadcast list
    And I schedule a campaign for that broadcast list 60 minutes from now with message "Later {{name}}"
    Then the response status should be 200
    And the JSON response field "state" should equal "SCHEDULED"
    And the JSON response should have a non-empty "scheduled_at" field

  @C3113
  Scenario: A scheduled campaign can be cancelled once, and nothing is sent
    When I upload "personalised_list.csv" as a new broadcast list
    And I schedule a campaign for that broadcast list 60 minutes from now with message "Later"
    And I cancel that campaign
    Then the response status should be 200
    And the JSON response field "state" should equal "CANCELLED"
    When I cancel that campaign
    Then the response status should be 409

  @C3114
  Scenario: A send time in the past is rejected
    When I upload "personalised_list.csv" as a new broadcast list
    And I schedule a campaign for that broadcast list in the past
    Then the response status should be 400

  @C3115
  Scenario: A placeholder no list column can fill is rejected
    When I upload "personalised_list.csv" as a new broadcast list
    And I send a campaign now to that broadcast list with message "Hi {{nickname}}"
    Then the response status should be 400

  @C3116
  Scenario: A file with a message column sends each row's own text
    When I quick-send "per_row_messages.csv" as a bulk campaign with no message
    Then the response status should be 200
    And the JSON response field "recipient_count" should be the number 2
    And every message of that campaign should read one of "Your parcel ABC123 is ready for collection" or "Reminder: meeting moved to 3pm"

  @C3117
  Scenario: A numbers-only file needs a message
    When I quick-send "numbers_only.csv" as a bulk campaign with no message
    Then the response status should be 400

  @C3118
  Scenario: Campaign messages are paginated and exportable as CSV
    When I upload "personalised_list.csv" as a new broadcast list
    And I send a campaign now to that broadcast list with message "Hello {{name}}"
    And I request that campaign's messages with limit 2
    Then the response status should be 200
    And the JSON response should be a list of exactly 2 items
    And the response header "X-Total-Count" should equal "3"
    When I export that campaign's messages as CSV
    Then the response status should be 200
    And the response body should contain "message_reference,phone_number,message,status"

  @C3119
  Scenario: A client without credit is refused with 402
    Given I am authenticated as a client administrator for a new client with no credit
    And a sender has been provisioned for my client
    When I send a campaign now to the numbers "254712345678, 254798765432" with message "No credit"
    Then the response status should be 402
