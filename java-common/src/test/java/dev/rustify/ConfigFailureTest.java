package dev.rustify;
import java.nio.file.*;
/** A malformed optional config must never prevent fallback initialization. */
public final class ConfigFailureTest {
 public static void main(String[] args) throws Exception {
  Path game=Files.createTempDirectory("rustify-invalid-config-");
  Files.createDirectories(game.resolve("config"));
  Files.writeString(game.resolve("config/rustify.properties"), "invalid=\\uXXXX\n");
  Engine.initialize(game,"test","test","test");
  if(!Files.exists(game.resolve("rustify/benchmarks/latest.json")))throw new AssertionError("Initialization failed after malformed config");
  java.util.Properties config=new java.util.Properties();
  if(Capabilities.snapshot(config,true).values().stream().anyMatch(c->c.state()!=Capabilities.State.DISABLED_BY_CONFIG))throw new AssertionError("unsafe defaults");
  config.setProperty("rustify.chunk.bitStorage","true");
  if(Capabilities.snapshot(config,true).get("rustify.chunk.bitStorage").state()!=Capabilities.State.UNSUPPORTED)throw new AssertionError("unimplemented hook enabled");
  if(Capabilities.snapshot(config,false).get("rustify.chunk.bitStorage").state()!=Capabilities.State.DISABLED_BY_FAILURE)throw new AssertionError("unavailable native enabled");
  System.out.println("Malformed optional config fallback passed");
 }
}
