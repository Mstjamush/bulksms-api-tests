Feature: Message templates and placeholder preview

  Background:
    Given I am authenticated as a client administrator for a new client

  @C3100
  Scenario: Creating a template
    When I create a template with body "Hi {{name|there}}, your balance is KES {{amount}}"
    Then the response status should be 200
    And the JSON response should have a non-empty "id" field

  @C3101
  Scenario: Template names are unique per client
    When I create a template with body "First"
    And I create another template with the same name
    Then the response status should be 409

  @C3102
  Scenario: Preview renders each recipient's own values, with defaults for missing ones
    When I upload "personalised_list.csv" as a new broadcast list
    And I preview the message "Hi {{name}}, please pay KES {{amount|the balance}}" against that broadcast list
    Then the response status should be 200
    And a preview sample should read "Hi Wanjiku, please pay KES 1500"
    And a preview sample should read "Hi Achieng, please pay KES the balance"

  @C3103
  Scenario: Preview flags placeholders no list column can fill
    When I upload "personalised_list.csv" as a new broadcast list
    And I preview the message "Hi {{nickname}}" against that broadcast list
    Then the preview should report unknown placeholder "nickname"

  @C3104
  Scenario: Emoji switch a message to UCS-2 with 70 characters per page
    When I preview the message "Karibu 🎉 12345678901234567890123456789012345678901234567890123456789012345678901"
    Then the preview template should be "UCS-2" with 2 pages
