# Rustify

Rustify is a shared Rust engine with a Java/JNI compatibility layer and thin Fabric, NeoForge and Forge adapters. It is a development foundation, not a released performance mod. No Minecraft algorithms are replaced by default and no startup/FPS improvement is claimed.

## Targets

- Fabric: Minecraft 26.3, Fabric Loader 0.20.1, API 0.165.0+26.3, stable Loom 1.19.3. Java 25 / Gradle 9.8.2.
- NeoForge: Minecraft 1.21.11 / NeoForge 21.11.48. Java 21.
- Forge: Minecraft 1.20.1 / Forge 47.4.28. Java 17.

See [verified status](docs/STATUS.md) before treating any adapter as compiled or launch-tested.

## Features and defaults

Shared components include ABI handshake, native self-test, fail-safe Java packing fallback, bootstrap profiler, bounded zlib experiments, parallel JAR scanner and content-validated SHA-256 cache. Scanner and cache are standalone tools; they do not replace loader discovery. Experimental game features are OFF. World generation and world storage remain untouched.

On first initialization, `config/rustify.properties` is created. `general.native=false` disables native loading. Individual experimental keys are documented in [compatibility](docs/COMPATIBILITY.md); setting an unsupported key does not install a game hook.

## Build and tests

Install stable Rust with rustfmt and clippy, and the target's JDK/Gradle. Then:

```sh
scripts/check.sh
./gradlew -p java-common build
scripts/build-loader.sh fabric
scripts/build-loader.sh neoforge
scripts/build-loader.sh forge
cargo run --manifest-path rust/Cargo.toml --release --locked -p rustify-bench
python3 scripts/summarize-benchmarks.py
```

The loader helper reuses prepared tools in the managed environment or falls back to installed Gradle. It preserves TLS verification and system CA trust. Native library packaging uses `java-common/src/main/resources/natives/{os}-{arch}/`; only copy libraries built from this project's trusted source. CI stages five platform libraries before producing adapter JARs. Generated build directories are ignored.

## Installation and fallback

There is no validated release artifact yet. Once release verification is complete, install only the JAR matching your Minecraft/loader combination in `mods/`. Trusted release builds are intended to include Windows x86_64, Linux x86_64/ARM64 and macOS x86_64/ARM64 natives. Only Linux x86_64 has been tested locally; configured CI jobs do not establish runtime support until they pass.

No native binaries are downloaded when Minecraft starts. Missing/incompatible native libraries are logged, native acceleration stays disabled, and Java/default behavior remains. Status and benchmark command adapters require loader validation; do not use benchmark commands on production servers.

## Diagnostics

Profiles are written beneath the game directory at `rustify/benchmarks/latest.json` and `latest.txt`. Unobservable startup phases are explicitly unmeasured. If native loading fails, inspect the platform/architecture and ABI diagnostic, try disabling `general.native`, and report the matching Java/native build versions. Corrupt standalone cache data is treated as a miss. Do not delete worlds as a troubleshooting step.

Microbenchmarks are synthetic and do not prove Minecraft performance improvements. See [benchmarks](docs/BENCHMARKS.md), [architecture](docs/ARCHITECTURE.md), [compatibility](docs/COMPATIBILITY.md) and [decisions](docs/DECISIONS.md).
