# Rustify status

This is a tested shared foundation, **not a release-ready Minecraft mod**. Work is on `dev/rustify-foundation`. No game optimization is enabled by default; no startup/FPS advantage is claimed.

| Component | Fabric | NeoForge 1.21.11 | Forge 1.20.1 |
|---|---|---|---|
| Builds | BLOCKED at plugin resolution | BLOCKED at plugin resolution | BLOCKED at plugin resolution |
| Launch tested | NO | NO | NO |
| Native JNI | Shared Linux x86_64 integration passes; adapter unverified | Same | Same |
| Startup profiler | Shared bootstrap/native timing tested; loader hooks unverified | Same | Same |
| Cache | Standalone Rust implementation tested; not integrated | Same | Same |
| Archive scanner | Parallel CLI tested; not integrated | Same | Same |
| Chunk acceleration | Java/Rust packing equivalence tested; game replacement OFF | Same | Same |
| Compression | Rust zlib roundtrip tested; game replacement OFF | Same | Same |
| Benchmarked | Shared JNI/packing and Rust compression only; game unavailable | Same | Same |
| Known issues | Latest stable target/Loom not confirmed; provisional 26.3 template | No compiled/launch proof | Requested 47.4.28 not resolved |

## Verified locally

- `cargo fmt --all --check`, `cargo clippy --all-targets -- -D warnings`, `cargo test --locked`: pass. Eight Rust tests cover packing widths/lengths/invalid input, bounded zlib, valid/invalid ZIP metadata, missing files, content invalidation and corrupt cache rebuilding.
- Rust JNI release library builds on Linux x86_64. Java 17-compatible common code compiles and Gradle `build` succeeds using JDK 21 / Gradle 8.14.3.
- Actual JVM/native ABI handshake, version retrieval, self-test, 192 deterministic Java/native comparisons and invalid native arguments pass. Missing packaged library falls back without failing initialization. Repeatable check script removes its own generated test classes before testing fallback.
- Parallel scanner inspected the common JAR: first run had zero content matches, second run one match; archive security/loader checks are never bypassed. Fingerprints are recomputed on every scan.
- Five local iterations each of JNI ping and batch packing are recorded. Rust-only packing and zlib roundtrip measured. No statistically controlled end-to-end advantage demonstrated.
- Gradle archive SHA-256 and downloaded Temurin JDK SHA-256 were verified against their official release checksums.

## Blockers and remaining work

The environment proxy denied `meta.fabricmc.net`, `maven.fabricmc.net`, `maven.neoforged.net` and `maven.minecraftforge.net` with HTTP 403. All adapter builds were actually attempted and failed at plugin resolution, before source compilation. Required domains are saved in the environment draft; saving does not apply them. Review/save environment settings and publish to activate the updated configuration, then retry version metadata and loader builds. TLS and checksum verification remain enabled.

Fabric versions derive from the current official example master, which uses snapshot Loom. This does not satisfy the requested latest **stable** selection yet. Resolve stable metadata after networking is available; do not publish the provisional adapter. NeoForge CI follows its official Gradle 9.2.1 wrapper; Fabric CI follows official Gradle 9.8.2 wrapper. These CI versions have not run locally. Forge uses the requested exact Minecraft and Forge versions. Java 17 for Forge and Java 25 for Fabric must be available to those builds.

CI definitions cover five native platforms, common Java, separate loader builds and benchmark artifacts. Only Linux x86_64 was actually exercised. CI has not been executed on GitHub. It builds native resources before bundling them into loader JARs; local loader builds currently omit natives unless the developer stages trusted project-built native resources in `java-common/src/main/resources/natives/`.

Unfinished: full observable startup lifecycle hooks, real feature enablement/conflict registry, loader-integrated batch scanner/cache/resource index, Java/native compression comparison and memory measurement, palette compaction, serialization, direct buffers, worldgen and Mixin metadata experiments. Native execution-only timing and complete handshake-call byte/call accounting are unavailable. Commands exist in adapter source but are not compiled or functionally tested. No development Minecraft server/client was launched. Existing benchmark commands execute synchronously and are development-only.

Next sequence: activate network settings; verify/pin stable Fabric dependencies and compatible JDK/Gradle; compile each adapter and repair actual API differences; launch isolated dev servers; verify commands/profile paths/native fallback; then measure candidate optimizations against real baselines. Keep all world-affecting paths off until correctness and compatibility tests pass.
