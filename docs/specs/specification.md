# helper-lib: Implementation Specification

Date: 2025-10-11

Overview
- This module provides concrete implementations of the helper-lib-api interfaces (see docs/external/architecture.helper-lib-api.md).
- Main package: `com.helperlib.command` with subpackages per CommandType.
- Depends on helper-lib-core for execution utilities, registry, JSON handling, and stream logging.
  - Types under `com.helperlib.core` are from the external core dependency.
  - API interfaces/abstracts are under `com.helperlib.api` (transitively provided by core).

Architecture and design
- Command pattern: Each command extends `com.helperlib.api.command.Command` and carries a `CommandMetadata` subclass.
- Async-first execution: `executeAsync()` runs on a virtual-thread executor from `com.helperlib.core.command.CommandExecutorService`.
  - Some commands also expose small sync helpers internally but the API contract is async.
- Factories and JPMS: Each command type has a `CommandFactory` that can (de)serialize metadata and create commands.
  - Factories are registered via `module-info.java` with `provides com.helperlib.api.command.CommandFactory with ...` enabling ServiceLoader use.
- Stream handling: Optional `StreamHandler` is passed to commands for stdout/stderr consumption and logging.
  - A `NoOpStreamHandler` from core is used as default when logging is not needed.
- Error model: All commands return `CommandResult(success, exitCode, executionTimeMs)`; failures typically map to `exitCode = -1`, interruption to 130 where applicable.
- Configuration: Higher-level configuration (command catalog, JSON persistence, registry) is hosted in core; this module focuses on concrete command behavior.

Common behaviours across commands
- Respect metadata for name/description/type and operation-specific fields.
- Measure execution time and report it in `CommandResult`.
- Avoid blocking callers: operations run in the shared virtual-thread pool.
- Use `StreamHandler` when provided to emit useful output, otherwise keep side effects minimal.

Command types
1) Terminal (package `com.helperlib.command.terminal`)
   - Purpose: Execute a system process/command with arguments and optional PATH adjustments.
   - Key pieces: `TerminalCommand`, `TerminalCommandMetadata`, `TerminalProcessExecutor`.
   - Behavior: Spawns a process, wires stdout/stderr to the `StreamHandler`, returns exit code; measures runtime.

2) Clipboard (package `com.helperlib.command.clipboard`)
   - Purpose: Copy text into the system clipboard.
   - Key pieces: `ClipboardCommand`, `ClipboardCommandMetadata`, `ClipboardService`.
   - Behavior: Uses a shared clipboard service to set clipboard content. Empty-string input is treated as a specific failure case. Logs via `StreamHandler` when enabled.

3) REST (package `com.helperlib.command.rest`)
   - Purpose: Perform an HTTP request (GET/POST/PUT/DELETE), optionally copy a field from the JSON response to the clipboard.
   - Key pieces: `RestCommand`, `RestCommandMetadata`.
   - Behavior: Builds an HttpClient request with headers/body, executes it, forwards a summary and body to `StreamHandler`, extracts a JSON path if configured (`toClipboard`), and copies the selected content (or full body) to clipboard. Success if status is 2xx; exitCode is the HTTP status or -1 on exceptions.

4) Terminal Toggle (package `com.helperlib.command.terminaltoggle`)
   - Purpose: A terminal command that supports a second "toggle" action (e.g., start/stop or enable/disable) via `ToggleCommand` API.
   - Key pieces: `TerminalToggleCommand`, `TerminalToggleCommandMetadata`, `ToggleCommandRegistry` (helper), `TerminalProcessExecutor`.
   - Behavior: Maintains concurrent-safe state using atomics. `executeAsync()` runs the main action; `toggleAsync()` constructs a temporary terminal metadata from toggle text and runs it. Prevents overlapping runs (separate gates for main vs toggle). Reports interruptions as exit code 130.

5) Tunnel Toggle (package `com.helperlib.command.tunneltoggle`)
   - Purpose: Manage an SSH local port forward (open/close) with username/password or key auth.
   - Key pieces: `TunnelToggleCommand`, `TunnelToggleCommandMetadata`, `TunnelAuthType`.
   - Behavior: Implements `ToggleCommand`. `executeAsync()` opens a tunnel using JSch and blocks until cancellation/stop; `toggleAsync()` attempts to close/flip the state (e.g., remove forwarding). Tracks running state via atomics and uses timeouts/latches for orderly shutdown. Returns `CommandResult` with success and timings.

Notes and conventions
- Threading: All long-running operations use the core executor. Toggle commands guard re-entrancy with atomic flags.
- Logging: Prefer `StreamHandler` for command output; some informational messages are printed to stdout/stderr when needed.
- Extensibility: New command types should follow the same pattern—metadata class, command class, factory implementation, and JPMS service registration.
