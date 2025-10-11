# helper-lib

A small Java library of executable commands (terminal, clipboard, REST, and tunnel toggle). It builds on an external core/API stack:
- All types under `com.helperlib.core` come from dependency:
  <dependency>
    <groupId>com.helperlib</groupId>
    <artifactId>helper-lib-core</artifactId>
    <version>1.2.14</version>
  </dependency>
- Interfaces under `com.helperlib.api` are provided transitively by the core dependency.

Stack
- Language: Java 24 (JPMS module: `module helper.lib`)
- Build: Maven
- Tests: JUnit 5, WireMock, Apache MINA SSHD
- Logging: SLF4J

Requirements
- JDK 24
- Maven 3.9+

Setup and build
- Install: `mvn clean install`
- Run tests: `mvn test`
- Package JAR: `mvn package` (copies runtime deps to `target/dependencies`)

Entry points and usage
- This is a library. Command implementations are exported in:
  `com.helperlib.command.terminal`, `clipboard`, `rest`, `terminaltoggle`, `tunneltoggle`.
- Factories are registered via `provides com.helperlib.api.command.CommandFactory` (usable with ServiceLoader).

Scripts
- Maven goals: clean, test, package, install. No custom scripts.

Environment variables
- None required. TODO: document if commands add configurable env vars later.

Tests
- Unit/integration tests live under `src/test/java` and use WireMock/embedded SSHD.

Project structure
- `src/main/java` – library code, `module-info.java` defines exports and services
- `src/test/java` – tests