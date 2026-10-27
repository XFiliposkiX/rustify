package dev.rustify;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Honest capability reporting: a config request never installs an absent game hook. */
public final class Capabilities {
    public enum State {
        AVAILABLE, ENABLED, DISABLED_BY_CONFIG, DISABLED_BY_COMPATIBILITY,
        DISABLED_BY_BENCHMARK, DISABLED_BY_FAILURE, EXPERIMENTAL, UNSUPPORTED
    }
    public record Capability(State state, String reason) {}
    private Capabilities() {}
    public static Map<String, Capability> snapshot(Properties config, boolean nativeLoaded) {
        Map<String, Capability> result = new LinkedHashMap<>();
        for (String feature : new String[]{"startup.archiveScanner", "startup.cache", "chunk.palette",
                "chunk.bitStorage", "compression", "worldgen.experimental", "memory.experimental"}) {
            boolean requested = Boolean.parseBoolean(config.getProperty("rustify." + feature, "false"));
            Capability capability = !requested
                    ? new Capability(State.DISABLED_BY_CONFIG, "safe default; no game hook enabled")
                    : !nativeLoaded
                    ? new Capability(State.DISABLED_BY_FAILURE, "native initialization unavailable")
                    : new Capability(State.UNSUPPORTED, "game integration not implemented or proven; Java retained");
            result.put("rustify." + feature, capability);
        }
        return java.util.Collections.unmodifiableMap(result);
    }
}
