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
