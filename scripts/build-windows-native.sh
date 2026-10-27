#!/usr/bin/env bash
# Cross-build the Windows x86_64 JNI DLL with the official Rust gnullvm target.
# LLVM_MINGW_ROOT must point to a trusted llvm-mingw UCRT distribution.
set -euo pipefail
cd "$(dirname "$0")/.."

: "${LLVM_MINGW_ROOT:?Set LLVM_MINGW_ROOT to the extracted llvm-mingw directory}"
linker="$LLVM_MINGW_ROOT/bin/x86_64-w64-mingw32-clang"
if [[ ! -x "$linker" ]]; then
  echo "Missing trusted llvm-mingw linker: $linker" >&2
  exit 2
fi

rustup target add x86_64-pc-windows-gnullvm
export CARGO_TARGET_X86_64_PC_WINDOWS_GNULLVM_LINKER="$linker"
export RUSTFLAGS='-C target-feature=+crt-static -C link-arg=-static'
cargo build --manifest-path rust/Cargo.toml --release --locked \
  --target x86_64-pc-windows-gnullvm -p rustify-jni

dll=rust/target/x86_64-pc-windows-gnullvm/release/rustify_jni.dll
destination=java-common/build/native-resources/natives/windows-x86_64
mkdir -p "$destination"
cp "$dll" "$destination/rustify_jni.dll"
sha256sum "$destination/rustify_jni.dll"
