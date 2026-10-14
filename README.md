# protomap-test-kotlin

A synthetic code base of the fictional company Acme for testing ProtoMap: 190 gRPC services with 1437 methods in 10
domains, and 5249 protobuf messages with 22778 fields, many nested several levels deep.

It was generated, with the eight other `protomap-test-*` repositories, from one catalog of Acme's APIs. The repositories
own disjoint domains and call each other's services, so each holds copies of the protobuf files it needs from the
others, byte for byte as their owners have them.

## Layout

The Kotlin 2/JVM transport platform is a Maven reactor. `contracts/` compiles the vendored `proto/` APIs into separate
Java and Kotlin artifacts per domain: Java messages, protobuf-kotlin DSL builders, and grpc-kotlin coroutine stubs are
produced at build time; generated bindings stay under `target/`. `domains/` holds the ten teams' endpoints, request
mappers, workflows, JDBC audit repositories, and Koin modules. `platform/` supplies configuration, gRPC status handling,
typed ports, JSON and reflection adapters, and process-owned channel workers. `application/` registers every service
behind the identity interceptor and contains reconciliation jobs and an operator replay CLI. Build with Java 21 or newer
and Maven 3.9 using `mvn -q package`. Apply `platform/src/main/resources/db/001_transport_journal.sql` to PostgreSQL
before starting the server. Configure `JDBC_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `MESH_TARGET`,
`IDENTITY_TARGET`, `GRPC_PORT`, and `ACME_SERVICE_TOKEN`, the credential of the server's own calls, through the
deployment environment. Channel worker calls belong to the server program; only the data crossing their process-owned
mailboxes is marked `known_gap` in the oracle.

The domains are `driverapp`, `fleet`, `drivers`, `routing`, `dispatch`, `telematics`, `maintenance`, `fuel`,
`deliveries`, `yards`. It calls services of `admissions`, `appointments`, `assets`, `audiences`, `audit`,
`authorization`, `billing`, `boxoffice`, `broadcasts`, `budgeting`, `careplans`, `cart`, `catalog`, `certificates`,
`chat`, `checkout`, `community`, `compliance`, `concessions`, `configuration`, `contracts`, `corporateevents`,
`customers`, `datasets`, `devices`, `documents`, `edgegateway`, `employees`, `expenses`, `fanportal`, `featureflags`,
`featurestore`, `identity`, `insurance`, `inventory`, `itsm`, `keymanagement`, `learning`, `ledger`, `lending`,
`licensing`, `lineage`, `localization`, `media`, `metrics`, `modelregistry`, `notifications`, `onboarding`, `orders`,
`patientapp`, `patients`, `payroll`, `performance`, `performers`, `personalization`, `pipelines`, `prescriptions`,
`ratelimiting`, `recruiting`, `returns`, `risk`, `search`, `seating`, `secrets`, `shipping`, `shows`, `sponsorships`,
`subscriptions`, `suppliers`, `surveys`, `telehealth`, `ticketing`, `tours`, `travel`, `treasury`, `venues`,
`warehousing`, `webhooks`, `workplaces` too.

## Oracle

`expected.json` lists what scanning the repository must find: every handler, every call a handler or a program makes,
and every field-level flow from where a handler has data to where a call or its response takes it, each tagged with the
shape of the code that makes it. `crates/graph/tests/fixture_coverage.rs` in `protomap-source` checks a scan against it.

The code mixes in, on purpose, what makes static analysis of real code bases hard. Flows by pattern:

| Pattern | Flows |
|---|---|
| `any_roundtrip` | 577 |
| `async_each` | 241 |
| `bidi_stream_call` | 4 |
| `channel_each` | 225 |
| `channel_worker` | 526 |
| `client_stream_call` | 14 |
| `companion_factory` | 594 |
| `constructor_stub` | 584 |
| `deadline_scope` | 574 |
| `delegated_port` | 592 |
| `domain_model` | 554 |
| `dsl_builder` | 1017 |
| `dsl_copy` | 572 |
| `extension_builder` | 562 |
| `flow_each` | 255 |
| `flow_pipeline` | 592 |
| `function_reference` | 568 |
| `json_roundtrip` | 589 |
| `lambda_property` | 554 |
| `lazy_client` | 538 |
| `object_registry` | 594 |
| `oneof_when` | 295 |
| `paginated_batch_job` | 3 |
| `pagination_cursor` | 3 |
| `receiver_lambda` | 580 |
| `reflection_copy` | 618 |
| `request_echo` | 2087 |
| `scope_functions` | 610 |
| `sealed_command` | 574 |
| `server_stream_call` | 446 |

Flows whose values pass through something on their way, by what:

| Transform | Flows |
|---|---|
| `domain_mapping` | 394 |
| `json` | 413 |
| `oneof_case` | 71 |
| `protobuf_any` | 404 |
| `reflection` | 438 |

Calls by pattern:

| Pattern | Calls |
|---|---|
| `any_roundtrip` | 126 |
| `async_each` | 66 |
| `bidi_stream_call` | 2 |
| `channel_each` | 67 |
| `channel_worker` | 122 |
| `client_stream_call` | 5 |
| `companion_factory` | 129 |
| `constructor_stub` | 123 |
| `deadline_scope` | 123 |
| `delegated_port` | 124 |
| `domain_model` | 122 |
| `dsl_builder` | 224 |
| `dsl_copy` | 126 |
| `extension_builder` | 125 |
| `flow_each` | 67 |
| `flow_pipeline` | 129 |
| `function_reference` | 121 |
| `identity_interceptor` | 1437 |
| `json_roundtrip` | 126 |
| `lambda_property` | 117 |
| `lazy_client` | 116 |
| `object_registry` | 131 |
| `oneof_when` | 57 |
| `paginated_batch_job` | 6 |
| `receiver_lambda` | 124 |
| `reflection_copy` | 129 |
| `scope_functions` | 126 |
| `sealed_command` | 123 |
| `server_stream_call` | 127 |
