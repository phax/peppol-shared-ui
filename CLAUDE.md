# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-module Maven project providing shared UI components for Peppol-related websites (e.g., peppol.helger.com). Built on the proprietary ph-oton UI framework. Part of the broader [phax/peppol](https://github.com/phax/peppol) ecosystem.

## Build Commands

```bash
# Build all modules
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests for a single module
mvn test -pl peppol-ui-types

# Run a single test class
mvn test -pl peppol-shared-validation -Dtest=VESRegistryTest
```

## Module Dependency Chain

Modules listed in dependency order (each depends on those above it):

1. **peppol-ui-types** — Core data types: SML configuration, SMP query params, nice-name mappings, feedback callbacks, ID type code lists
2. **peppol-ui** — UI helpers: certificate display, nice-name rendering, SML config UI, select components
3. **peppol-shared-api** — REST API components: rate-limited API endpoints, JSON helpers, AJAX handlers for Peppol operations
4. **peppol-shared-ui** — Full page implementations: participant information pages, SMP query pages, shared UI config
5. **peppol-shared-as4** — AS4 message transmission UI pages
6. **peppol-shared-validation** — Document validation UI: VES registry, validation controllers and configuration

## Key Dependencies

- **ph-oton** (photon) — Proprietary web UI framework (Bootstrap 4 variant)
- **peppol-commons** — Peppol protocol types, SMP client, SML operations
- **phase4** — AS4 message exchange (used by peppol-shared-as4)
- **phive-rules** — Validation rules engine (used by peppol-shared-validation)
- **ph-commons** — Foundation library (collections, XML, IO, etc.)
- **ratelimitj** — In-memory rate limiting for API endpoints

## Package Structure

All source is under `com.helger.peppol`:
- `ui.types` — Core types (peppol-ui-types)
- `ui` — UI components (peppol-ui)
- `api` — REST API layer (peppol-shared-api)
- `sharedui` — Shared pages and UI (peppol-shared-ui)
- `as4` — AS4 pages (peppol-shared-as4)
- `validate` — Validation (peppol-shared-validation)

## Code Conventions

- Uses JSpecify annotations for nullability
- Uses `forbiddenapis` Maven plugin to enforce API usage rules
- Apache License 2.0 header required on all source files
- This is a library project (no main class) — consumed by downstream Peppol web applications
