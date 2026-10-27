#![forbid(unsafe_code)]
use std::{io::{Read,Seek},path::{Path,PathBuf}};
/// Inspection only; never extracts entries or substitutes loader validation.
pub fn metadata<R:Read+Seek>(reader:R)->Result<Vec<(String,u64,u64)>,zip::result::ZipError>{
 let mut zip=zip::ZipArchive::new(reader)?;if zip.len()>100_000 {return Err(zip::result::ZipError::InvalidArchive("entry limit".into()));}
 let mut out=Vec::with_capacity(zip.len());for i in 0..zip.len(){let e=zip.by_index_raw(i)?;out.push((e.name().to_owned(),e.size(),e.compressed_size()));}Ok(out)
}
pub fn discover(root:&Path)->std::io::Result<Vec<PathBuf>> {
 let mut pending=vec![root.to_path_buf()];let mut files=Vec::new();while let Some(dir)=pending.pop(){for entry in std::fs::read_dir(dir)? {let entry=entry?;let ty=entry.file_type()?;if ty.is_dir(){pending.push(entry.path());}else if ty.is_file() && entry.path().extension().is_some_and(|x|x.eq_ignore_ascii_case("jar") || x.eq_ignore_ascii_case("zip")){files.push(entry.path());}}}files.sort();Ok(files)
}
#[cfg(test)] mod tests {use super::*;#[test] fn rejects_invalid(){for n in 0..128 {assert!(metadata(std::io::Cursor::new(vec![0;n])).is_err());}}}
