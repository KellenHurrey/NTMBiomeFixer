package com.kellen.ntmbiomefixer.mixins.hbm;


import com.falsepattern.endlessids.mixin.helpers.ChunkBiomeHook;
import com.hbm.main.MainRegistry;
import com.kellen.ntmbiomefixer.PacketDispatcher;
import com.hbm.world.WorldUtil;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldUtil.class, remap = false)
public abstract class WorldUtilMixin {

    /**
     * @author Kellen
     * @reason Update biome ids
     */
    @Inject(method = "Lcom/hbm/world/WorldUtil;setBiome(Lnet/minecraft/world/World;IILnet/minecraft/world/biome/BiomeGenBase;)V", at = @At("HEAD"), cancellable = true)
    private static void setBiome(World world, int x, int z, BiomeGenBase biome, CallbackInfo ci){
        MainRegistry.logger.debug("here");
        int relBlockX = x & 15;
        int relBlockZ = z & 15;
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        ((ChunkBiomeHook) chunk).getBiomeShortArray()[relBlockZ << 4 | relBlockX] = (short)(biome.biomeID);
        chunk.isModified = true;
        ci.cancel();
    }

    /**
     * @author Kellen
     * @reason crash
     */
    @Overwrite
    public static void syncBiomeChange(World world, int x, int z) {
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        PacketDispatcher.wrapper.sendToAllAround(new ChangedBiomeSyncPacket(x >> 4, z >> 4, ((ChunkBiomeHook) chunk).getBiomeShortArray()), new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, 128, z, 1024D));
    }

    /**
     * @author Kellen
     * @reason update biome sync packet
     */
    @Overwrite
    public static void syncBiomeChangeBlock(World world, int x, int z) {
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        short biome = ((ChunkBiomeHook) chunk).getBiomeShortArray()[(z & 15) << 4 | (x & 15)];
        PacketDispatcher.wrapper.sendToAllAround(new ChangedBiomeSyncPacket(x, z, biome), new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, 128, z, 1024D));
    }

    /**
     * @author Kellen
     * @reason Crash
     */
    @Overwrite
    public static void syncBiomeChange(World world, Chunk chunk) {
        /* "let's not make all this valuable information accessible, at all, hehe hoho huehue" -mojank, probably */
		/*if(!(world instanceof WorldServer)) return;
		WorldServer server = (WorldServer) world;
		PlayerManager man = server.getPlayerManager();
		Method getOrCreateChunkWatcher = ReflectionHelper.findMethod(PlayerManager.class, man, new String[] {"getOrCreateChunkWatcher"}, int.class, int.class, boolean.class);
		int x = chunk.getChunkCoordIntPair().chunkXPos;
		int z = chunk.getChunkCoordIntPair().chunkZPos;
		PlayerManager.PlayerInstance playerinstance = (PlayerInstance) getOrCreateChunkWatcher.invoke(man, x, z, false);*/

        /* this sucks ass */
        ChunkCoordIntPair coord = chunk.getChunkCoordIntPair();
        PacketDispatcher.wrapper.sendToAllAround(new ChangedBiomeSyncPacket(coord.chunkXPos, coord.chunkZPos, ((ChunkBiomeHook) chunk).getBiomeShortArray()), new NetworkRegistry.TargetPoint(world.provider.dimensionId, coord.getCenterXPos(), 128, coord.getCenterZPosition() /* who named you? */, 1024D));
    }
}
