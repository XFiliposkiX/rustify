#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
cargo fmt --manifest-path rust/Cargo.toml --all --check
cargo clippy --manifest-path rust/Cargo.toml --all-targets -- -D warnings
cargo test --manifest-path rust/Cargo.toml --locked
cargo build --manifest-path rust/Cargo.toml --release -p rustify-jni
rm -rf java-common/build/check
mkdir -p java-common/build/check
javac --release 17 -d java-common/build/check java-common/src/main/java/dev/rustify/*.java java-common/src/test/java/dev/rustify/*.java
java -cp java-common/build/check dev.rustify.IntegrationTest
case "$(uname -s)-$(uname -m)" in
 Linux-x86_64) platform=linux-x86_64; lib=librustify_jni.so;;
 Linux-aarch64) platform=linux-aarch64; lib=librustify_jni.so;;
 Darwin-arm64) platform=macos-aarch64; lib=librustify_jni.dylib;;
 Darwin-x86_64) platform=macos-x86_64; lib=librustify_jni.dylib;;
 *) echo 'Use CI for Windows native integration'; exit 0;;
esac
mkdir -p "java-common/build/check/natives/$platform"
cp "rust/target/release/$lib" "java-common/build/check/natives/$platform/"
java -cp java-common/build/check dev.rustify.IntegrationTest native
