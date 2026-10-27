use rayon::prelude::*;
use rustify_cache::{Context, Index};
use std::{path::PathBuf, time::Instant};
fn main() -> Result<(), Box<dyn std::error::Error>> {
    let args: Vec<_> = std::env::args().collect();
    if args.len() != 7 {
        return Err("usage: rustify-core ROOT CACHE MC LOADER LOADER_VERSION THREADS".into());
    }
    let threads: usize = args[6].parse()?;
    if !(1..=32).contains(&threads) {
        return Err("threads must be 1..32".into());
    }
    let pool = rayon::ThreadPoolBuilder::new()
        .num_threads(threads)
        .build()?;
    let root = PathBuf::from(&args[1]);
    let cache = PathBuf::from(&args[2]);
    let context = Context {
        minecraft: args[3].clone(),
        loader: args[4].clone(),
        loader_version: args[5].clone(),
    };
    let old = rustify_cache::load(&cache, &context);
    let start = Instant::now();
    let paths = rustify_archive::discover(&root)?;
    // Errors remain explicit per file. No loader validation is skipped, even on a hit.
    let rows:Vec<_>=pool.install(||paths.par_iter().map(|path|{
  match rustify_cache::fingerprint(path){Ok(entry)=>{
   let hit=old.as_ref().is_some_and(|index|index.entries.iter().any(|e|e.path==entry.path && e.sha256==entry.sha256 && e.length==entry.length));
   let metadata=std::fs::File::open(path).ok().and_then(|f|rustify_archive::metadata(f).ok());
   let report=serde_json::json!({"path":path,"sha256":entry.sha256,"length":entry.length,"cacheContentMatch":hit,"archive":metadata});(Some(entry),report)
  },Err(e)=>(None,serde_json::json!({"path":path,"error":e.to_string()}))}
 }).collect());
    let (entries, reports): (Vec<_>, Vec<_>) = rows.into_iter().unzip();
    let index = Index {
        version: 1,
        context,
        entries: entries.into_iter().flatten().collect(),
    };
    // Scan output stays useful when cache storage is unavailable.
    if let Err(e) = rustify_cache::save(&cache, &index) {
        eprintln!("cache write unavailable; continue without cache: {e}");
    }
    println!(
        "{}",
        serde_json::json!({"elapsedNs":start.elapsed().as_nanos(),"threads":threads,"files":reports})
    );
    Ok(())
}
