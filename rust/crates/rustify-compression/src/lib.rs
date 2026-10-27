#![forbid(unsafe_code)]
use std::io::{Read, Write};
pub fn compress(input: &[u8]) -> std::io::Result<Vec<u8>> {
 let mut out=flate2::write::ZlibEncoder::new(Vec::new(),flate2::Compression::default()); out.write_all(input)?;out.finish()
}
pub fn decompress(input: &[u8], limit: usize) -> std::io::Result<Vec<u8>> {
 let decoder=flate2::read::ZlibDecoder::new(input); let mut out=Vec::new(); decoder.take(limit.saturating_add(1) as u64).read_to_end(&mut out)?;
 if out.len()>limit {return Err(std::io::Error::other("decompression limit"));} Ok(out)
}
#[cfg(test)] mod tests {use super::*;#[test] fn roundtrip(){let v=vec![42;65536];let c=compress(&v).unwrap();assert_eq!(decompress(&c,v.len()).unwrap(),v);assert!(decompress(&c,10).is_err());assert!(decompress(b"invalid",100).is_err());}}
