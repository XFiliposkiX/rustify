# Verified status

Development branch: `dev/rustify-foundation`. Changes are pushed to the connected GitHub repository. This is a tested foundation, not a finished performance mod. No Minecraft algorithms are replaced by default.

| Component | Fabric 26.3 | NeoForge 1.21.11 | Forge 1.20.1 |
|---|---|---|---|
| Builds | COMPILES; full build passed | COMPILES; JAR produced | COMPILES; full build passed |
| Launch tested | PARTIAL: server bootstrap, stops at EULA | NOT VERIFIED: assets blocked | PARTIAL: server launcher stops at EULA |
| Functionally tested | NO: commands/world not exercised | NO | NO |
| Native JNI | TESTED inside Fabric bootstrap | Shared JVM test only | Shared JVM test only |
| Fallback verified | TESTED with native disabled in config | Shared JVM test only | Shared JVM test only |
| Startup profiler | Bootstrap/native phases recorded | Shared implementation; not observed in game | Shared implementation; not observed in game |
| Cache | Rust CLI/tests; loader integration unfinished | Same | Same |
| Archive scanner | Parallel Rust CLI tested; not loader replacement | Same | Same |
| Chunk acceleration | Exact Java/Rust packing comparisons; game replacement OFF | Same | Same |
| Compression | Bounded Rust zlib tests; game replacement OFF | Same | Same |
| Worldgen experiments | NOT IMPLEMENTED | NOT IMPLEMENTED | NOT IMPLEMENTED |
| Benchmarked | Shared microbenchmarks; one native/one fallback bootstrap sample | Shared microbenchmarks only | Shared microbenchmarks only |
| Known issues | Client assets denied; EULA gate; no world/command validation | Assets denied; no launch/native-in-game proof | Standard runServer downloads client assets; EULA gate |

## Evidence

- Official metadata confirms stable Fabric Minecraft 26.3, Loader 0.20.1, API 0.165.0+26.3. Stable Loom 1.19.3 uses Java 25 and Gradle 9.8.2. Actual compilation required `implementation` and the current `Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)` API.
- NeoForge 21.11.48 targets exactly 1.21.11. ModDevGradle 2.0.148 resolved and compiled using Java 21 / Gradle 8.14.3. The official template uses Gradle 9.2.1 (CI configuration). Permission API was independently checked in its generated Minecraft sources and fixed.
- Forge 1.20.1-47.4.28 compiled and reobfuscated with Java 17 / Gradle 8.14.3. No target substitution.
- All three generated JARs were inspected and contain this project's Linux x86_64 JNI library. Other platforms are configured in CI but not locally verified.
- Rust formatting, Clippy with warnings denied, eight Rust tests, native release build, 192 Java/native packing comparisons, invalid JNI arguments and standalone missing-library fallback pass on Linux x86_64. Java-common Gradle build passed previously; shared source was preserved.
- Actual Fabric server bootstrap discovers Rustify, records nativeLoaded=true, ABI 1, native version 0.1.0 and successful self-test. A second invocation with general.native=false records nativeLoaded=false and continues to the EULA gate. Artifacts are in benchmarks/fabric/. Neither invocation reaches world readiness or verifies commands.
- Forge server bootstrap with `runServer -x downloadAssets` reaches its EULA gate. Exclusion avoids client textures that a dedicated server does not use; native initialization did not occur before this gate. Standard runServer fails downloading client assets.
- Fabric client launch was attempted and fails at asset downloads before the client process starts. NeoForge server launch likewise requests blocked assets; excluding downloadAssets fails its required-input validation, so that route is not supported or claimed successful.

## External limitations

`resources.download.minecraft.net` responds CONNECT 403. Its domain is added to the saved environment draft; saving does not apply or publish policy. Activate the saved network configuration before retrying clients and standard loader run tasks. Maven access now works with proxy settings and host Java CA trust; the previous blanket Maven blocker is resolved.

Servers require user acceptance of Minecraft's EULA. No EULA acceptance or world-ready launch is claimed. After accepting terms locally, rerun isolated development servers and test `/rustify status` and benchmark commands; stop cleanly. Never use experimental synchronous benchmarks on production servers.

Git push works. GitHub Actions configuration is pushed and eligible for push triggers. Both gh and HTTPS access to api.github.com return 403, so remote job outcomes are UNAVAILABLE, not green. No release publication was performed.

## Remaining engineering

Full feature capability/conflict registry, lifecycle timing beyond bootstrap, loader-integrated scanner/cache/resource index, batch scanner JNI, native execution-only accounting, comparative compression/memory benchmarks, palette compaction, serialization, native handles, worldgen and Mixin experiments remain unfinished. No measured end-to-end performance win justifies enabling game replacements. Do not promote experimental paths based on source presence or compile success.

Reproduce: scripts/check.sh; scripts/build-loader.sh fabric; scripts/build-loader.sh neoforge; scripts/build-loader.sh forge. Helpers select prepared target JDKs/Gradle and preserve TLS validation. For manual launches use the corresponding helper with runServer/runClient. See DECISIONS.md and BENCHMARKS.md for provenance and limitations.

## Latest continuation (supersedes earlier network observations)

All three targets were rebuilt successfully after resilience/diagnostic changes. Shared Rust/JNI checks and Java-common `clean build` pass, including a new malformed-properties recovery test and capability-default tests. Status now lists explicit per-feature states and reasons. `/rustify benchmark startup` reads the measured bootstrap report; `/rustify cache` explicitly reports that loader cache integration is unavailable. Command compilation is verified, live command execution is not.

Client asset access recovered during retry. Fabric actually started the client and discovered Rustify, then crashed in Minecraft SDL initialization with `Unable to initialize SDL: No available video device`. This machine has no available renderer. The earlier asset-policy failure is no longer the final Fabric client blocker. A graphical menu, world and rendering remain unverified. Server EULA remains unaccepted and full world-ready launches remain unverified.

`scripts/build-loader.sh` now stages this workspace's locally built native library automatically. `scripts/package-artifacts.py` verifies shared Engine and packaged native entries, collects all three development candidate JARs under `build/candidates/`, and writes SHA-256 hashes and source revision to manifest.json. These are development candidates, not validated production releases. Other native platforms and CI outcomes remain unverified. No performance replacement is enabled by default.

A Windows 11 Fabric fallback diagnostic kit can be generated at
`build/rustify-fabric-0.1.0-dev-windows-diagnostics.zip`. It includes a verified
Fabric JAR, test instructions, hashes, and a PowerShell collector for the latest
Minecraft log, newest crash report, Rustify config/profile, and non-secret system
metadata. It intentionally contains no Windows native library until that target is
built and runtime-tested. Its purpose is to verify safe fallback and collect the
evidence needed for Windows native work; it is not a performance release.

The fallback kit was exercised on Windows 11 amd64 with Minecraft 26.3,
Fabric Loader 0.20.1, Java 25.0.1, and an NVIDIA RTX 3060. Rustify was
discovered, reported the missing Windows library, initialized resources, reached
the main menu, and closed normally. The measured Rustify bootstrap was 10.50 ms;
native detection/fallback was 0.72 ms. These are single diagnostic measurements,
not performance comparisons.

A Windows x86_64 UCRT DLL is now cross-built using Rust's official
`x86_64-pc-windows-gnullvm` target and llvm-mingw 20261020. Its PE format, imports,
and all five JNI exports were inspected; SHA-256 is
`2ddc24f0e2e933501a65281ec288b8c84eadd7e9b307e4538fff2ca3e3df0f5d`.
It is packaged in `build/rustify-fabric-0.1.0-dev-windows-native-test.zip`.
Runtime JNI compatibility remains unverified until the Windows menu test reports
`nativeLoaded=true`, matching ABI 1, and a passed self-test.
