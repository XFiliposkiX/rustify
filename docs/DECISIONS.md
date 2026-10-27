# Decisions

| Choice | Reason | Alternatives | Changeable |
|---|---|---|---|
| Shared Rust workspace and Java 17 common source | One engine, compatible with Forge's runtime | Three independent engines rejected | Yes, ABI version must change for incompatible bridge changes |
| Separate Gradle projects per loader | Isolates incompatible plugins and toolchains | Single root with all plugins | Yes |
| JNI ABI 1 | Required, portable, actual integration test | Panama deferred | Yes |
| All game accelerations OFF | No demonstrated end-to-end advantage or game compatibility | Default-on experiments rejected | Only after measured correctness and advantage |
| SHA-256 content revalidation | Timestamps and file sizes are insufficient | Metadata-only cache rejected | Yes, preserve correctness |
| zip crate and flate2 | Established parsers, bounded output decompression | Handwritten central directory and codecs | Yes |
| Forge 1.20.1 / 47.4.28 | Requested exact target | Other 47.x only if verified incompatibility | Yes with evidence |
| NeoForge 1.21.11 / 21.11.48, ModDevGradle 2.0.148 | Official NeoForgeMDKs/MDK-1.21.11-ModDevGradle main template inspected | Other versions pending metadata access | Yes |
| Fabric 26.3 / Loader 0.20.1 / API 0.165.0+26.3 / Loom 1.19-SNAPSHOT | Official FabricMC/fabric-example-mod master template at inspection time | Older remapped Minecraft templates | PROVISIONAL: metadata access is blocked; latest stable status and stable Loom replacement unresolved |
| Fabric CI Java 25 / Gradle 9.8.2 | Gradle version from official Fabric master wrapper; not locally verified | Older Gradle 8 wrapper for other targets | Yes; compatibility must be verified before release |
| MIT project code | Conventional permissive foundation | Apache-2.0 | Yes, subject to distributed versions |

Official templates were fetched through raw.githubusercontent.com on 2026-10-27. Their moving branches do not prove latest stable versions. Loader builds were attempted and failed during plugin resolution under denied Maven access. Do not treat their versions or APIs as verified by compilation.

No user technical decisions were required. No new worktree, remote writes, or downloaded third-party native engine binaries were used.

NeoForge official MDK wrapper specifies Gradle 9.2.1; CI uses that version. Local plugin-resolution attempts used 8.14.3 and do not establish compatibility with its required plugin.

## Continuation: stable Fabric metadata

Official Fabric metadata now responds: Minecraft 26.3 is stable, Loader 0.20.1 stable and API 0.165.0+26.3 exists. Loom is pinned to stable 1.19.3 instead of the snapshot. Actual resolution confirmed Loom requires Java 25; a checksummed official Temurin 25+36 JDK is used locally. Gradle 9.8.2 successfully configures Loom. Host Java trust is selected through `/etc/ssl/certs/java/cacerts` to preserve proxy CA trust; verification is never disabled. Alternatives were remaining on snapshot tooling or choosing an older Minecraft release; both rejected. Versions remain changeable after compatibility validation.
