package dev.rustify.fabric;
import dev.rustify.Engine;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
public final class RustifyFabric implements ModInitializer {
 public void onInitialize() {
  var loader=FabricLoader.getInstance();Engine.initialize(loader.getGameDir(),"fabric",loader.getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString(),loader.getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString());
  CommandRegistrationCallback.EVENT.register((dispatcher, access, environment)->dispatcher.register(Commands.literal("rustify").then(Commands.literal("status").executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.status()),false);return 1;})).then(Commands.literal("benchmark").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.benchmark("all")),false);return 1;}).then(Commands.literal("jni").executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.benchmark("jni")),false);return 1;})).then(Commands.literal("chunks").executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.benchmark("chunks")),false);return 1;})).then(Commands.literal("compression").executes(c->{c.getSource().sendSuccess(()->Component.literal(Engine.benchmark("compression")),false);return 1;})))));
 }
}
