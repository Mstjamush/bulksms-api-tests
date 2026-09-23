# bulksms-api – REST Assured BDD Tests

[![bulksms-api-tests](https://github.com/YOUR_GH_ORG/bulksms-api-tests/actions/workflows/tests.yml/badge.svg)](https://github.com/YOUR_GH_ORG/bulksms-api-tests/actions/workflows/tests.yml)
<!-- Badge points at a placeholder org/repo until this project is pushed to GitHub - see "Continuous Integration" below. -->

Automated API test suite for [`bulksms-api`](https://github.com/Mstjamush/bulksms-api), built with **Cucumber 7**, **REST Assured 5**, and **JUnit 4** . Every request and scenario is logged to console and to a file (see [Logs](#logs)), and a GitHub Actions pipeline runs the suite against a real bulksms-api on every push (see [Continuous Integration](#continuous-integration)).

It covers both halves of the API, positive and negative scenarios:

Two ways to run it against **TestRail**:
1. **`mvn test`** — runs everything, and reports each `@C<id>`-tagged scenario's result to a TestRail run you created manually (same pattern as `Leave_Manager_RestAssured_BDDTests`).
2. **`TestRailDrivenRunner`** — the fuller pipeline: **reads** the case list straight from your TestRail project, **creates** a run for exactly those cases, **executes** only the scenarios tagged for them, and **updates** TestRail with each result as it finishes.

---

## Prerequisites

| Tool | Minimum version |
|------|------------------|
| Java JDK | 17 |
| Apache Maven | 3.8+ |
| `bulksms-api` | running and accessible, with MySQL **and RabbitMQ** reachable (see its own README) |
| TestRail account | with API access enabled |

---

## Setup

### 1. Start bulksms-api

In `bulksms-api/` (see its README for full detail):

```bash
mysql -uroot -pr00t sms < schema.sql && mysql -uroot -pr00t sms < ../sms_platform_schema.sql   # fresh DB only
python -m app.migrate                   # every time bulksms-api changes - applies migrations/*.sql
docker compose up -d rabbitmq           # optional - see "Run without RabbitMQ"
source .venv/bin/activate && pip install -r requirements.txt
cp .env.example .env
python seed_admin.py                    # once - bootstraps the Super Administrator
uvicorn app.main:app --port 8000        # the API
python -m app.scheduler                 # scheduled campaigns (a second terminal)
```

`seed_admin.py` always creates the same bootstrap login (`admin@bulksms-platform.com` / `ChangeMe123!`), which is already the default in this suite's config below — you only need to change it if you've since changed that account's password.

### 2. Configure `testrail.properties`

Edit `src/test/resources/testrail.properties`:

```properties
testrail.url=https://YOUR_INSTANCE.testrail.io
testrail.username=your.email@company.com
testrail.api_key=your_testrail_api_key_here   # TestRail -> My Settings -> API Keys
testrail.project_id=1
testrail.suite_id=                            # only if your project uses multiple suites
testrail.run_id=                              # only needed for the "mvn test" flow, see below

api.base.url=http://localhost:8000
auth.api_key=dev-secret                       # must match bulksms-api's AUTH_API_KEY
auth.test_mode=true                           # informational - mirrors the API's AUTH_TEST_MODE

admin.super_admin.email=admin@bulksms-platform.com
admin.super_admin.password=ChangeMe123!
```

### 3. Install dependencies

```bash
mvn dependency:resolve
```

---

## Coverage

| Feature file | Cases | Covers |
|---|---|---|
| `single_sms.feature`, `bulk_sms.feature` | C3010-C3016, C3020-C3029 | public single send; bulk upload in all three file shapes (`phone_number,message` · template over `phone_number,name,amount` · `phone_number` + one message), `.xls`, background outcome checked through the uploads report |
| `public_api_client.feature` | C3130-C3136 | sends as a known, billed client (`X-Api-Client`): charged pages x rate, own-sender rule, unknown client, `phone_number` alias, invalid number, 402 without credit |
| `admin_broadcast_lists.feature` | C3090-C3098 | list upload, placeholders from columns, **duplicates reported with row and reason** (repeated in file / already on list), import history + skipped-rows CSV, `.xls`, bad files, paging, client isolation |
| `admin_templates.feature` | C3100-C3104 | templates, unique names, preview rendering with defaults, unknown placeholders, UCS-2 page counting |
| `admin_campaigns.feature` | C3110-C3119 | send-now and scheduled campaigns, estimate = pages x rate, cancel once, past `send_at`, per-row message files, paging + CSV export, 402 without credit |
| `admin_billing.feature` | C3120-C3127 | idempotent top-ups, client can't top up itself, plan fee + allowance + rate, bundles (units + remainder), bundle request approve/reject, soonest-expiring credit spent first, ledger |
| `admin_reports.feature` | C3140-C3144 | overview (with billing), daily points, daily CSV, `X-Total-Count` paging |
| `admin_*` (auth, clients, users, senders, bulk campaigns), `health.feature` | C3001, C3030-C3081 | the original admin and health coverage |

Every client the suite provisions gets KES 1000 of credit from the Super Administrator (billing is enforced, so an unfunded client is refused with 402); scenarios that need an unfunded client say so ("…for a new client with no credit"). Upload fixtures live in `src/test/resources/testdata/`; `loans.xls` is a real legacy-Excel file built by `tools/make_xls.py` (no Excel or extra library needed to regenerate it).

## Mapping scenarios to TestRail cases

Every scenario worth reporting is tagged `@C<id>`. The ids currently in the feature files (`C3001`, `C3010`–`C3016`, `C3020`–`C3024`, `C3030`–`C3034`, `C3040`–`C3045`, `C3050`–`C3054`, `C3060`–`C3064`, `C3070`–`C3072`, `C3080`–`C3081`) are **placeholders** — they won't mean anything until they match real case ids in your TestRail project.

Two ways to align them:

- **Create the cases in TestRail first** with these exact titles (see each `.feature` file's `Scenario:` lines), then find-and-replace the placeholder tag with the real id TestRail assigns, e.g. `@C3010` → `@C482`.
- **Run `TestRailDrivenRunner --dry-run`** (below) to print every case TestRail already has in the configured project/suite, then re-tag scenarios to match the ones you want this suite to own.

Scenarios with no `@C<id>` tag still run, they just aren't reported to TestRail.

---

## Running the tests

### Run everything

```bash
mvn test
```

### Run one case / one area

```bash
mvn test -Dcucumber.filter.tags="@C3010"
mvn test -Dcucumber.filter.tags="@C3010 or @C3011 or @C3012"
```

### Run without RabbitMQ (e.g. local dev on this machine - see Known limitations)

```bash
mvn test -Dcucumber.filter.tags="not @ignore and not @requires-rabbitmq"
```

### Skip TestRail reporting (dry run, no TestRail side-effects)

```bash
mvn test -Dtestrail.disabled=true
```

For plain `mvn test` runs, results are posted against the fixed `testrail.run_id` in `testrail.properties` — create that run in TestRail first (include the cases you tagged above), copy its numeric id from the run's URL (`/runs/view/456` → `456`).

---

## The TestRail-driven pipeline

This is the "read cases from TestRail, execute them, update TestRail" flow end to end, with no manually-created run needed:

```bash
# Preview only - lists the cases TestRail returns and the tag filter that
# would be used, without creating a run or executing anything:
mvn test-compile exec:java -Dexec.mainClass=bulksmsapi.testrail.TestRailDrivenRunner -Dexec.args="--dry-run"

# The real thing:
mvn test-compile exec:java -Dexec.mainClass=bulksmsapi.testrail.TestRailDrivenRunner
```

What it does ([`TestRailDrivenRunner`](src/test/java/bulksmsapi/testrail/TestRailDrivenRunner.java)):

1. **Read** — calls TestRail's `get_cases` for `testrail.project_id` (+ `testrail.suite_id`), paging through all of them.
2. **Run** — opens a new TestRail run scoped to exactly those case ids, builds a Cucumber tag filter (`@C<id1> or @C<id2> or ...`) from them, and runs [`TestRunner`](src/test/java/bulksmsapi/runners/TestRunner.java) (the same suite `mvn test` runs) filtered to just those tags.
3. **Update** — as each scenario finishes, [`TestRailHooks`](src/test/java/bulksmsapi/hooks/TestRailHooks.java) posts its result (Passed/Failed, with elapsed time) to the run created in step 2, via [`TestRailClient`](src/test/java/bulksmsapi/testrail/TestRailClient.java).

If 0 scenarios match, it's almost always because the feature files' `@C<id>` tags don't line up with real case ids yet — see "Mapping scenarios to TestRail cases" above.

---

## Test reports

After any run, reports land in `target/`:

| Report | Path |
|--------|------|
| HTML | `target/cucumber-html-report/index.html` |
| JSON | `target/cucumber.json` |

```bash
open target/cucumber-html-report/index.html
```

---

## Logs

Code calls **`org.jboss.logging.Logger`** ([`jboss-logging`](https://github.com/jboss-logging/jboss-logging)) throughout — the same facade the sibling [`bulksms`](../../Personal_Co_code/bulksms) Quarkus consumer uses (`LOG.infof("...%s...", value)` printf-style calls), for one consistent logging API across the whole bulksms ecosystem. At startup it auto-detects `org.slf4j.spi.LocationAwareLogger` on the classpath and delegates to SLF4J, so `slf4j-api` + `logback-classic` still do the actual work below (console + rotating file, configured in [`logback-test.xml`](src/test/resources/logback-test.xml)) - no separate bridge dependency needed. Every run logs to both:

| Where | What |
|-------|------|
| Console | One line per HTTP call (method, URI, status, duration) and per scenario start/finish, plus Cucumber's own `pretty` output. |
| `target/logs/bulksms-api-tests.log` | The same, but every run appends/rotates here (by day, capped at 200MB total) — this is what to attach when a run fails and the console scrollback is gone. CI uploads this directory as a build artifact (see below). |

Default level is INFO (method/URI/status/duration only). For request/response bodies too:

```bash
mvn test -Dbulksmsapi.http.log.level=DEBUG
# or, for this suite's own step/hook/TestRail-client logging:
mvn test -Dbulksmsapi.log.level=DEBUG
```

---

## Skipping scenarios

Tag any scenario `@ignore` to exclude it from `mvn test`'s default `not @ignore` filter:

```gherkin
@ignore
Scenario: Work in progress
  ...
```

`@C3013` (invalid-signature test) is tagged `@ignore` out of the box, since it needs `AUTH_TEST_MODE=false` on the API to observe a real `401` — see the comment in [`single_sms.feature`](src/test/resources/features/single_sms.feature).

---

## Continuous Integration

[`.github/workflows/tests.yml`](.github/workflows/tests.yml) runs on every push/PR to `main` and can also be triggered manually (with an option to run the TestRail-driven pipeline instead of plain `mvn test`). Two jobs:

1. **`build`** — `mvn test-compile` only. No external dependencies, always runs, catches compile-level breakage fast.
2. **`integration-test`** — the real thing: spins up **MySQL** and **RabbitMQ** as GitHub Actions service containers, checks out and boots a live `bulksms-api` (schema load → `pip install` → `seed_admin.py` → `uvicorn`), then runs this suite against it and uploads `target/logs/`, the Cucumber HTML/JSON reports, and Surefire reports as a build artifact (`if: always()`, so failed runs still get the artifact).

**Before this workflow can actually run:**
- Push both `bulksms-api-tests` and `bulksms-api` to GitHub — neither has a remote configured yet.
- Update the placeholder `repository: YOUR_GH_ORG/bulksms-api` in `tests.yml`'s "Checkout bulksms-api" step to the real path.

**Optional secrets** (only needed for the TestRail-driven `workflow_dispatch` run, or to have plain `mvn test` runs in CI report to a real TestRail run): `TESTRAIL_URL`, `TESTRAIL_USERNAME`, `TESTRAIL_API_KEY`, `TESTRAIL_PROJECT_ID`, `TESTRAIL_SUITE_ID`, `TESTRAIL_RUN_ID` — these map onto `testrail.properties` keys via the environment-variable override described in Setup step 2. Without them, TestRail reporting is skipped (a warning is logged per scenario) rather than failing the build.

---

## Known limitations

- **Three scenarios need RabbitMQ reachable** — `@C3010`/`@C3016` (single SMS success, with and without an explicit `senderId`) and `@C3080` (bulk campaign creation) — or bulksms-api returns `500`/`400` instead of `202`/`200` (see `app/mq.py`'s `get_channel()` and `app/admin/service.py`'s `_queue_campaign_messages`). All three are tagged `@requires-rabbitmq`. CI's `integration-test` job covers them via a RabbitMQ service container; for a local run without RabbitMQ, exclude them: `mvn test -Dcucumber.filter.tags="not @ignore and not @requires-rabbitmq"`.
- **`TestRailClient` covers only what this pipeline needs** (`get_cases`, `add_run`, `add_result_for_case`) — not the full TestRail API.
- **Multipart bulk-upload tests only exercise `.csv`.** `.xlsx`/`.xlsm` paths use the same `parse_file` dispatch in the API, so aren't separately covered here.
- **Sender/bulk-campaign coverage stops at creation.** `GET /senders` and `GET /bulk-campaigns` (listing) aren't separately asserted, and campaign delivery status isn't checked beyond the `202`/`200` creation response.
