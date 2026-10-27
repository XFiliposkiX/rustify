# Benchmark evidence

Raw locally measured results are in `benchmarks/`. Measurements are microbenchmarks, not Minecraft startup or FPS claims. Java results were collected on Linux x86_64 with the installed Java 21 runtime; Rust release builds used rustc 1.99.1. The Java harness runs five iterations (including cold iterations) of 100,000 JNI pings and 10,000 batches of 4,096 packed integers. Data, exact timings and mode labels remain in the raw artifacts. Fallback and JNI runs are separate JVMs, so these do not establish a reliable speedup. No optimization is enabled by default.

Rust benchmark measures packing and zlib roundtrip of a highly compressible synthetic 64 KiB payload. It does not compare Java compression or real chunk corpora. Memory consumption, native execution-only time, compression JNI transfer cost, loader discovery-equivalent timings and startup improvement are **unavailable**. There are no estimated speedup numbers.

Reproduce with `scripts/check.sh` and `cargo run --manifest-path rust/Cargo.toml --release --locked -p rustify-bench`. For publishable results, add warmup, alternating baselines, multiple forks, representative input corpora, allocation/RSS measurements and environment metadata. Native packing includes copying and allocation. Every cross-language packing result is compared with a deterministic Java reference. A failed correctness comparison must stop the benchmark.

`python3 scripts/summarize-benchmarks.py` summarizes recorded Java samples into `benchmarks/summary.json`: median, minimum, maximum and population standard deviation, preserving sample counts and measured labels. Separate JVM runs remain unsuitable for declaring a production speedup.
