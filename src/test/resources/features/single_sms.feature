Feature: POST /api/v1/sms - single SMS

  @C3010 @requires-rabbitmq
  Scenario: Sending a single SMS is accepted and queued
    Given a single SMS request to "254712345678" with message "Your OTP is 123456"
    When I submit the single SMS request
    Then the response status should be 202
    And the JSON response should have a non-empty "reference" field
    And the JSON response field "status" should equal "QUEUED"

  @C3011
  Scenario: Rejects a request missing the "to" field
    Given a single SMS request with no "to" field
    When I submit the single SMS request
    Then the response status should be 422

  @C3012
  Scenario: Rejects a request missing the "message" field
    Given a single SMS request with no "message" field
    When I submit the single SMS request
    Then the response status should be 422

  @C3014
  Scenario: Rejects a request with an empty "to" field
    Given a single SMS request to "" with message "Hello from the automated suite"
    When I submit the single SMS request
    Then the response status should be 422

  @C3015
  Scenario: Rejects a request with an empty "message" field
    Given a single SMS request to "254712345678" with message ""
    When I submit the single SMS request
    Then the response status should be 422

  @C3016 @requires-rabbitmq
  Scenario: Accepts a request with no senderId and falls back to the default sender
    Given a single SMS request to "254712345678" with message "No senderId supplied" and no senderId
    When I submit the single SMS request
    Then the response status should be 202
    And the JSON response field "status" should equal "QUEUED"

  # Ignored by default: AUTH_TEST_MODE=true (the API's default) bypasses
  # signing entirely, so this would fail out of the box. Set AUTH_TEST_MODE=false
  # on the API (and auth.test_mode=false in testrail.properties, informational
  # only), then run with -Dcucumber.filter.tags="@requires-signing" - see README.
  @C3013 @requires-signing @ignore
  Scenario: Rejects a request with an invalid X-Signature
    Given a single SMS request to "254712345678" with message "Signature check"
    When I submit the single SMS request with an invalid signature
    Then the response status should be 401
