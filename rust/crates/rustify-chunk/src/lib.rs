#![forbid(unsafe_code)]
/// Minecraft-style non-straddling storage, least significant slot first.
pub fn pack(values: &[u32], bits: u8) -> Result<Vec<u64>, &'static str> {
    if !(1..=32).contains(&bits) { return Err("bits must be 1..32"); }
    let slots = 64 / usize::from(bits);
    let mask = (1u64 << bits) - 1;
    let mut out = vec![0; values.len().div_ceil(slots)];
    for (i, &v) in values.iter().enumerate() {
        if u64::from(v) > mask { return Err("value exceeds width"); }
        out[i / slots] |= u64::from(v) << ((i % slots) * usize::from(bits));
    }
    Ok(out)
}
pub fn unpack(words: &[u64], count: usize, bits: u8) -> Result<Vec<u32>, &'static str> {
    if !(1..=32).contains(&bits) { return Err("invalid bits"); }
    let slots = 64 / usize::from(bits);
    if words.len() != count.div_ceil(slots) { return Err("invalid word count"); }
    let mask = (1u64 << bits) - 1;
    Ok((0..count).map(|i| ((words[i / slots] >> ((i % slots) * usize::from(bits))) & mask) as u32).collect())
}
#[cfg(test)] mod tests {
 use super::*;
 #[test] fn generated_roundtrips() { let mut seed=42u32; for bits in 1..=32 { for len in [0,1,7,63,64,65,4096] { let v:Vec<_>=(0..len).map(|_| {seed=seed.wrapping_mul(1664525).wrapping_add(1013904223); seed & (u32::MAX >> (32-bits))}).collect(); assert_eq!(unpack(&pack(&v,bits).unwrap(),len,bits).unwrap(),v); } } }
 #[test] fn rejects_invalid() {assert!(pack(&[2],1).is_err());assert!(pack(&[],0).is_err());assert!(unpack(&[],1,4).is_err());assert!(unpack(&[],0,33).is_err());}
}
