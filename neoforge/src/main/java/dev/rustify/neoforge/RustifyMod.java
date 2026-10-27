package dev.rustify.neoforge;
import dev.rustify.Engine;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
@Mod("rustify")
public final class RustifyMod {
 public RustifyMod() {Engine.initialize(java.nio.file.Path.of("."),"neoforge","1.21.11","21.11.48");net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::commands);}
 private void commands(RegisterCommandsEvent event) {
 var root=Commands.literal("rustify").then(Commands.literal("status").executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.status()),false);return 1;}));
 var bench=Commands.literal("benchmark").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.benchmark("all")),false);return 1;});
 for(String kind:new String[]{"jni","startup","compression","chunks"})bench.then(Commands.literal(kind).executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.benchmark(kind)),false);return 1;}));
 root.then(Commands.literal("cache").executes(c->{c.getSource().sendSuccess(()->Component.literal("Cache: standalone Rust index available; loader cache integration unavailable"),false);return 1;}));
 event.getDispatcher().register(root.then(bench));
 }
}
