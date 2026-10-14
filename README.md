# protomap-test-kotlin

A synthetic Kotlin code base for testing [ProtoMap](https://protomap.ai) by [HoundDog.ai](https://hounddog.ai). It is
part of the code of Acme, a fictional company.

The code mixes in, on purpose, what makes static analysis of real code bases hard, and `expected.json` is the answer key
of what a scanner should find in it.

Acme's code is spread over multiple repositories, one per programming language. They were generated together from one
catalog of Acme's APIs. Each owns its own domains and calls the others' services, so it holds copies of the protobuf
files it needs from them, byte for byte as their owners have them. Scan them together to follow calls and data from one
repository to another.

| Repository                                                                         | Language   |
|------------------------------------------------------------------------------------|------------|
| [protomap-test-csharp](https://github.com/hounddogai/protomap-test-csharp)         | C#         |
| [protomap-test-golang](https://github.com/hounddogai/protomap-test-golang)         | Go         |
| [protomap-test-java](https://github.com/hounddogai/protomap-test-java)             | Java       |
| [protomap-test-javascript](https://github.com/hounddogai/protomap-test-javascript) | JavaScript |
| **protomap-test-kotlin** (this one)                                                | Kotlin     |
| [protomap-test-python](https://github.com/hounddogai/protomap-test-python)         | Python     |
| [protomap-test-rust](https://github.com/hounddogai/protomap-test-rust)             | Rust       |
| [protomap-test-swift](https://github.com/hounddogai/protomap-test-swift)           | Swift      |
| [protomap-test-typescript](https://github.com/hounddogai/protomap-test-typescript) | TypeScript |

## Repository Layout

A Kotlin 2 transport platform on the JVM, built as a Maven reactor:

- `contracts/`: compiles the protobuf files in `proto/` into separate Java and Kotlin artifacts per domain: Java
  messages, protobuf-kotlin DSL builders, and grpc-kotlin coroutine stubs. They are generated under `target/` at build
  time and not committed.
- `domains/`: the teams' endpoints, request mappers, workflows, JDBC audit repositories, and Koin modules.
- `platform/`: configuration, gRPC status handling, typed ports, JSON and reflection adapters, and process-owned channel
  workers. The calls the channel workers make belong to the server program.
- `application/`: registers every service behind the identity interceptor, and holds reconciliation jobs and an operator
  replay CLI.

## Build and Run

Build with Java 21 or newer and Maven 3.9:

```sh
mvn -q package
```

Before starting the server, apply `platform/src/main/resources/db/001_transport_journal.sql` to PostgreSQL, and set
`JDBC_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `MESH_TARGET`, `IDENTITY_TARGET`, `GRPC_PORT`, and
`ACME_SERVICE_TOKEN`, the credential of the server's own calls.

## Added Code and Benchmark Labels

`tools/grow` (seed 20261116) added ordinary application code to this repository, and 16 programs whose jobs carry rpc
responses to other rpcs through constructs a static analysis cannot resolve. All of it is under
`application/src/main/kotlin/com/acme/transport/kit/`, `application/src/main/kotlin/com/acme/transport/linehaul/`,
`application/src/main/kotlin/com/acme/transport/lastmile/`, `application/src/main/kotlin/com/acme/transport/fleetops/`,
`application/src/main/kotlin/com/acme/transport/crewing/`, `application/src/main/kotlin/com/acme/transport/terminal/`,
`application/src/main/kotlin/com/acme/transport/freightpay/`, `application/src/main/kotlin/com/acme/transport/safety/`,
`application/src/main/kotlin/com/acme/transport/emissions/`, `application/src/main/kotlin/com/acme/transport/customs/`,
`application/src/main/kotlin/com/acme/transport/network/`, `application/src/main/kotlin/com/acme/transport/ops/`,
`application/src/test/kotlin/`.

- The ordinary code (domain models, validation rules, stores and caches, adjustment engines, reports, workflows, feature
  flags, retries, command-line tools, and unit tests) makes no gRPC calls, so it adds no flows.
- Each planted job reads one rpc's response and hands some of its fields to one or more implementations, each calling
  another rpc. Which implementations run is decidable from the code of the functions on the flow's path. A flow through
  one that runs is in `flows`, with a `pattern` ending in `_real`, or the plain pattern name when every implementation
  runs; one through an implementation that cannot run is in `forbidden_flows`, with the full source and target and a
  pattern ending in `_decoy`.
- `benchmark` in `expected.json` names the generator, its seed, the new patterns, the new code's path and function
  prefixes, and the planted programs' islands. The labels are complete for the new code: every real flow it creates is
  in `flows`, so a flow the scanner reports in the new code that is not there is false.
- New `flows` entries are `known_gap` unless the scanner already finds them. Unlike the other `forbidden_flows`, which
  only name a source, the `_decoy` entries name an endpoint pair; `fixture_coverage.rs`, which reads only their source
  and target kind, lists the ones the scanner reports.

| Pattern | What decides which implementation runs | True | False |
|---|---|---:|---:|
| `direct_call` | nothing: one concrete call, which the scanner finds certain | 53 | 0 |
| `dispatch_closure_map` | a map of closures, one called by a constant key | 80 | 144 |
| `dispatch_filter_loop` | a loop over keyed implementations that skips all but one key | 76 | 139 |
| `dispatch_func_value` | a function value a switch on a constant kind reassigns | 72 | 113 |
| `dispatch_index` | a list of implementations, one taken by a constant index | 80 | 148 |
| `dispatch_registry` | a registry of implementations looked up by a constant key | 127 | 251 |
| `dispatch_runtime` | run-time data, so each implementation may run | 72 | 0 |
| `dispatch_switch` | a selector's switch on a kind its caller passes as a constant | 138 | 242 |
| `fanout_all` | nothing: a loop runs every implementation | 63 | 0 |
