# Compatibility and configuration

No Minecraft behavior, archive security checks, world storage, chunk pipeline, compression paths, Sponge Mixin processing, or world generation are replaced. Compatibility with Sodium, Lithium, FerriteCore, ModernFix and other mods has not been launch-tested. Current compatibility strategy is passive instrumentation and explicitly invoked experiments only.

`config/rustify.properties` is created conservatively. `general.native=false` disables native loading. Registry keys are `rustify.startup.archiveScanner`, `rustify.startup.cache`, `rustify.chunk.palette`, `rustify.chunk.bitStorage`, `rustify.compression`, `rustify.worldgen.experimental`, `rustify.memory.experimental`; all are false. Setting experimental keys to true does not currently install game hooks. Status reports them disabled. A production feature registry with supported enablement and conflict decisions remains unfinished.

Commands are scaffolded equivalently on the three adapters: `/rustify status`, `/rustify benchmark`, and `jni`, `compression`, `chunks` subcommands. Benchmarks require permission level 2 and run synchronously; do not run them on a production server. Compression command explicitly reports the missing Java/JNI comparison. Command APIs remain uncompiled because dependency resolution was blocked.

World generation, serialization replacement, native direct-buffer ownership and bytecode/Mixin transformation acceleration are deferred. No seed behavior is changed.
