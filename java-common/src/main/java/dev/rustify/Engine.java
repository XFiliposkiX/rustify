package dev.rustify;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** One shared compatibility layer. No loader or Minecraft classes enter this module. */
public final class Engine {
    public static final String VERSION = "0.1.0";
    private static final String[] FEATURES = {"startup.archiveScanner", "startup.cache", "chunk.palette", "chunk.bitStorage", "compression", "worldgen.experimental", "memory.experimental"};
    private static final AtomicLong CALLS = new AtomicLong(), IN = new AtomicLong(), OUT = new AtomicLong(), TIME = new AtomicLong();
    private static volatile boolean loaded;
    private static String nativeVersion = "unavailable", reason = "not initialized", loader = "unknown", minecraft = "unknown";
    private static Path game;
    private static Properties config;
    private Engine() {}
    public static synchronized void initialize(Path directory, String loaderName, String mc, String loaderVersion) {
        if (game != null) return;
        game = directory; loader = loaderName; minecraft = mc;
        long bootstrap = System.nanoTime();
        config = new Properties();
        try {
            Path file = game.resolve("config/rustify.properties");
            if (Files.exists(file)) try (InputStream in = Files.newInputStream(file)) {config.load(in);}
            else {Files.createDirectories(file.getParent()); for (String f : FEATURES) config.setProperty("rustify."+f, "false"); config.setProperty("general.native", "true"); try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW)) {config.store(out, "Rustify: experiments are OFF; no game behavior replaced");}}
        } catch (IOException e) {System.err.println("Rustify config unavailable: " + e);}
        long start = System.nanoTime();
        if (Boolean.parseBoolean(config.getProperty("general.native", "true"))) {
            try {
                loadPackaged();
                if (NativeBridge.abi() != 1) throw new LinkageError("native ABI mismatch");
                nativeVersion = Objects.requireNonNull(NativeBridge.version());
                if (NativeBridge.selfTest() != 1) throw new LinkageError("native self-test failed");
                loaded = true; reason = "handshake passed; game replacements disabled";
            } catch (LinkageError | Exception e) {loaded = false; reason = e.toString(); System.err.println("Rustify native disabled; vanilla behavior retained: " + reason);}
        } else reason = "disabled by configuration";
        try {writeProfile(bootstrap, System.nanoTime()-start, loaderVersion);} catch(IOException e) {System.err.println("Rustify profile write failed: " + e);}
    }
    private static void loadPackaged() throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        os = os.contains("win") ? "windows" : os.contains("mac") ? "macos" : os.contains("linux") ? "linux" : "unsupported";
        String arch = System.getProperty("os.arch");
        arch = arch.equals("amd64") || arch.equals("x86_64") ? "x86_64" : arch.equals("aarch64") || arch.equals("arm64") ? "aarch64" : "unsupported";
        String name = os.equals("windows") ? "rustify_jni.dll" : os.equals("macos") ? "librustify_jni.dylib" : "librustify_jni.so";
        try (InputStream in = Engine.class.getResourceAsStream("/natives/"+os+"-"+arch+"/"+name)) {
            if (in == null) throw new FileNotFoundException("no packaged native for " + os + "-" + arch);
            // Private fresh directory prevents stale or attacker-controlled extraction targets.
            Path dir = Files.createTempDirectory("rustify-native-");
            Path target = dir.resolve(name); Files.copy(in, target); target.toFile().deleteOnExit(); dir.toFile().deleteOnExit();
            System.load(target.toAbsolutePath().toString());
        }
    }
    public static long[] referencePack(int[] values, int bits) {
        if(bits<1 || bits>32) throw new IllegalArgumentException("bits");
        int slots=64/bits; long mask=(1L<<bits)-1;
        long[] out=new long[(values.length+slots-1)/slots];
        for(int i=0;i<values.length;i++){long v=Integer.toUnsignedLong(values[i]);if(v>mask)throw new IllegalArgumentException("value");out[i/slots]|=v<<((i%slots)*bits);} return out;
    }
    public static long[] experimentalPack(int[] values, int bits) {
        if(!loaded) return referencePack(values,bits);
        long start=System.nanoTime(); CALLS.incrementAndGet(); IN.addAndGet(values.length*4L);
        try {long[] out=NativeBridge.pack(values,bits); OUT.addAndGet(out.length*8L);return out;}
        catch (LinkageError | RuntimeException e) {loaded=false;reason=e.toString();System.err.println("Rustify feature disabled: "+reason);return referencePack(values,bits);}
        finally {TIME.addAndGet(System.nanoTime()-start);}
    }
    public static String status() {return "Rustify Java="+VERSION+" nativeLoaded="+loaded+" native="+nativeVersion+" ABI="+(loaded?1:"unavailable")+" loader="+loader+" Minecraft="+minecraft+" enabled=[] disabled="+Arrays.toString(FEATURES)+" compatibility=no Minecraft replacements; "+reason+" cache=not integrated JNI calls="+CALLS+" bytesIn="+IN+" bytesOut="+OUT+" boundaryWallNs="+TIME;}
    public static String benchmark(String kind) {
        if(kind.equals("jni")) {if(!loaded)return "JNI benchmark unavailable: "+reason;long start=System.nanoTime();for(int i=0;i<100000;i++){if(NativeBridge.ping(i)!=i)throw new AssertionError("JNI mismatch");}CALLS.addAndGet(100000);return "measured JNI ping 100000 calls ns="+(System.nanoTime()-start);}
        if(kind.equals("chunks")) {int[] values=new int[4096];for(int i=0;i<values.length;i++) values[i]=i%32;long[] expected=referencePack(values,5);long start=System.nanoTime();for(int i=0;i<10000;i++)if(!Arrays.equals(expected,experimentalPack(values,5)))throw new AssertionError("packing mismatch");return "measured packing (native="+loaded+") 10000 batches ns="+(System.nanoTime()-start);}
        if(kind.equals("compression")) return "Compression: isolated Rust benchmark available via cargo run --release -p rustify-bench; Java/JNI comparison unavailable";
        return status()+"; select jni, chunks, compression";
    }
    private static String escape(String s) {return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");}
    private static void writeProfile(long bootstrap, long loading, String loaderVersion) throws IOException {
        Path dir=game.resolve("rustify/benchmarks");Files.createDirectories(dir);
        String json="{\"timestamp\":\""+java.time.Instant.now()+"\",\"loader\":\""+escape(loader)+"\",\"loaderVersion\":\""+escape(loaderVersion)+"\",\"minecraft\":\""+escape(minecraft)+"\",\"java\":\""+escape(System.getProperty("java.version"))+"\",\"os\":\""+escape(System.getProperty("os.name")+" "+System.getProperty("os.arch"))+"\",\"processors\":"+Runtime.getRuntime().availableProcessors()+",\"bootstrapNs\":"+(System.nanoTime()-bootstrap)+",\"nativeLoadingNs\":"+loading+",\"unobserved\":[\"loader initialization\",\"mod discovery\",\"mod metadata discovery\",\"mixin preparation\",\"resource indexing\",\"registries\",\"resource reload\",\"client initialization\"]}";
        Files.writeString(dir.resolve("latest.json"),json);Files.writeString(dir.resolve("latest.txt"),status()+"\n"+json+"\n");
    }
}
