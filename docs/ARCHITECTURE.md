# Architecture

Minecraft → Fabric / NeoForge / Forge adapter → java-common Engine → versioned JNI → shared Rust workspace.

Adapters compile the same common source into their own artifact. Native libraries are resource files at `natives/{os}-{arch}/{library}`. CI builds each platform on its native runner, tests handshake and packing, and includes all native resources in adapter JARs. This workflow is configured, not yet remotely executed. There are no launch-time downloads. Extraction uses fresh private temporary directories and never reuses a cached executable.

ABI 1: `abi`, `version`, `selfTest`, `ping`, `pack`. Initialization validates version before exposing native operations. Failures leave default Java behavior. Experimental packing is batched, capped at 16,777,216 integers and checks width and values; no raw JNI buffer pointers or handwritten unsafe Rust are used. Array transfer and boundary wall time are counted for packing; ping benchmark is counted separately. Initialization calls and native execution-only time are not yet counted. JNI allocation failures are not promised recoverable from VM exhaustion.

The standalone scanner traverses without following symlinks, fingerprints with SHA-256 and parallelizes per-file inspection on a bounded pool. ZIP metadata uses the zip crate, never extraction. Resource names appear in archive entries; no game resource index is installed. Cache context includes Minecraft and loader versions; content is rehashed even on matching timestamps. A corrupt index is treated as a miss and replaced after scanning. Concurrent/stale temporary cache files cause cache writes to be skipped rather than risk overwriting another writer. Cache is experimental CLI functionality, not loader integration.

Bootstrap and native loading have real monotonic timings and wall timestamps in the game directory's `rustify/benchmarks/latest.{json,txt}`. Earlier loader phases cannot be timed from mod initialization. They are explicitly marked unobserved; future loader-specific instrumentation must provide real phase events.
