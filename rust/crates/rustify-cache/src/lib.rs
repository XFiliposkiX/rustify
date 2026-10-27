#![forbid(unsafe_code)]
use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use std::{
    io::{self, Read},
    path::{Path, PathBuf},
};
#[derive(Debug, PartialEq, Eq, Serialize, Deserialize)]
pub struct Context {
    pub minecraft: String,
    pub loader: String,
    pub loader_version: String,
}
#[derive(Serialize, Deserialize)]
pub struct Entry {
    pub path: PathBuf,
    pub length: u64,
    pub modified_ns: u128,
    pub sha256: String,
}
#[derive(Serialize, Deserialize)]
pub struct Index {
    pub version: u32,
    pub context: Context,
    pub entries: Vec<Entry>,
}
pub fn fingerprint(path: &Path) -> io::Result<Entry> {
    let mut file = std::fs::File::open(path)?;
    let before = file.metadata()?;
    let mut hash = Sha256::new();
    let mut buf = [0u8; 65536];
    loop {
        let n = file.read(&mut buf)?;
        if n == 0 {
            break;
        }
        hash.update(&buf[..n]);
    }
    let after = file.metadata()?;
    if before.len() != after.len() || before.modified()? != after.modified()? {
        return Err(io::Error::other("file changed during scan"));
    }
    Ok(Entry {
        path: path.to_path_buf(),
        length: after.len(),
        modified_ns: after
            .modified()?
            .duration_since(std::time::UNIX_EPOCH)
            .map_err(io::Error::other)?
            .as_nanos(),
        sha256: format!("{:x}", hash.finalize()),
    })
}
/// Content is always rehashed: timestamps alone never establish validity.
pub fn valid(entry: &Entry) -> bool {
    fingerprint(&entry.path)
        .is_ok_and(|now| now.length == entry.length && now.sha256 == entry.sha256)
}
pub fn load(path: &Path, context: &Context) -> Option<Index> {
    let index: Index = serde_json::from_slice(&std::fs::read(path).ok()?).ok()?;
    if index.version != 1 || &index.context != context {
        return None;
    }
    Some(index)
}
pub fn save(path: &Path, index: &Index) -> io::Result<()> {
    let data = serde_json::to_vec(index)?;
    let tmp = path.with_extension("tmp");
    let mut f = std::fs::OpenOptions::new()
        .write(true)
        .create_new(true)
        .open(&tmp)?;
    use std::io::Write;
    f.write_all(&data)?;
    f.sync_all()?;
    drop(f);
    std::fs::rename(tmp, path)
}
#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn malformed() {
        assert!(serde_json::from_slice::<Index>(b"{}").is_err());
    }
    #[test]
    fn missing() {
        assert!(fingerprint(Path::new("/missing-rustify")).is_err());
    }
}
