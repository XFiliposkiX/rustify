use jni::{
    objects::{JClass, JIntArray},
    sys::{jint, jlongArray, jstring},
    JNIEnv,
};
#[no_mangle]
pub extern "system" fn Java_dev_rustify_NativeBridge_abi(_: JNIEnv, _: JClass) -> jint {
    1
}
#[no_mangle]
pub extern "system" fn Java_dev_rustify_NativeBridge_version(env: JNIEnv, _: JClass) -> jstring {
    env.new_string(env!("CARGO_PKG_VERSION"))
        .map_or(std::ptr::null_mut(), |s| s.into_raw())
}
#[no_mangle]
pub extern "system" fn Java_dev_rustify_NativeBridge_selfTest(_: JNIEnv, _: JClass) -> jint {
    i32::from(rustify_chunk::pack(&[1, 2, 3], 4) == Ok(vec![801]))
}
#[no_mangle]
pub extern "system" fn Java_dev_rustify_NativeBridge_ping(
    _: JNIEnv,
    _: JClass,
    value: jint,
) -> jint {
    value
}
#[no_mangle]
pub extern "system" fn Java_dev_rustify_NativeBridge_pack(
    mut env: JNIEnv,
    _: JClass,
    input: JIntArray,
    bits: jint,
) -> jlongArray {
    let result = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        let len = env.get_array_length(&input).ok()?;
        if len > 16_777_216 || !(1..=32).contains(&bits) {
            return None;
        }
        let mut data = vec![0i32; len as usize];
        env.get_int_array_region(&input, 0, &mut data).ok()?;
        let values: Vec<_> = data.into_iter().map(|v| v as u32).collect();
        let packed = rustify_chunk::pack(&values, bits as u8).ok()?;
        let out = env.new_long_array(packed.len() as i32).ok()?;
        let signed: Vec<_> = packed.into_iter().map(|v| v as i64).collect();
        env.set_long_array_region(&out, 0, &signed).ok()?;
        Some(out.into_raw())
    }));
    match result {
        Ok(Some(out)) => out,
        _ => {
            if !env.exception_check().unwrap_or(true) {
                let _ = env.throw_new(
                    "java/lang/IllegalArgumentException",
                    "invalid native packing request",
                );
            }
            std::ptr::null_mut()
        }
    }
}
