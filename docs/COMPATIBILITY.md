# Compatibility and configuration

No Minecraft behavior, archive security checks, world storage, chunk pipeline, compression paths, Sponge Mixin processing, or world generation are replaced. Compatibility with Sodium, Lithium, FerriteCore, ModernFix and other mods has not been launch-tested. Current compatibility strategy is passive instrumentation and explicitly invoked experiments only.

`config/rustify.properties` is created conservatively. `general.native=false` disables native loading. Registry keys are `rustify.startup.archiveScanner`, `rustify.startup.cache`, `rustify.chunk.palette`, `rustify.chunk.bitStorage`, `rustify.compression`, `rustify.worldgen.experimental`, `rustify.memory.experimental`; all are false. Setting experimental keys to true does not currently install game hooks. Status reports them disabled. A production feature registry with supported enablement and conflict decisions remains unfinished.

Commands are scaffolded equivalently on the three adapters: `/rustify status`, `/rustify benchmark`, and `jni`, `compression`, `chunks` subcommands. Benchmarks require permission level 2 and run synchronously; do not run them on a production server. Compression command explicitly reports the missing Java/JNI comparison. Command APIs remain uncompiled because dependency resolution was blocked.

World generation, serialization replacement, native direct-buffer ownership and bytecode/Mixin transformation acceleration are deferred. No seed behavior is changed.

Continuation verification: Fabric 26.3, NeoForge 21.11.48 / Minecraft 1.21.11, and Forge 47.4.28 / Minecraft 1.20.1 now compile. Fabric bootstrap verifies JNI and configuration-disabled fallback, but does not reach world readiness. NeoForge/Forge native-in-game initialization and all cross-mod interactions remain unverified. See STATUS.md for launch gates; earlier command-source compilation limitations are superseded by actual successful builds.

Latest diagnostics include explicit capability state/reason reporting. Malformed optional properties are caught and logged rather than aborting initialization. Native extraction cleanup registration was fixed so the file is removed before its temporary directory. Native platform selection recognizes Darwin as macOS. Actual Fabric client startup now reaches SDL and fails because the environment has no video device, rather than failing asset resolution.
