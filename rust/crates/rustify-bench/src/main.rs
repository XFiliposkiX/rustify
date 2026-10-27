use std::time::Instant;
fn main() {
    let values: Vec<_> = (0..4096).map(|i| i % 32).collect();
    let start = Instant::now();
    for _ in 0..10000 {
        std::hint::black_box(rustify_chunk::pack(std::hint::black_box(&values), 5).unwrap());
    }
    println!(
        "{{\"experiment\":\"rust_pack_4096\",\"iterations\":10000,\"elapsed_ns\":{}}}",
        start.elapsed().as_nanos()
    );
    let input = vec![42; 65536];
    let start = Instant::now();
    for _ in 0..100 {
        let c = rustify_compression::compress(&input).unwrap();
        assert_eq!(
            rustify_compression::decompress(&c, input.len()).unwrap(),
            input
        );
    }
    println!(
        "{{\"experiment\":\"zlib_roundtrip_64k\",\"iterations\":100,\"elapsed_ns\":{}}}",
        start.elapsed().as_nanos()
    );
}
