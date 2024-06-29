package com.kellen.ntmbiomefixer.mixins.hbm;


import com.falsepattern.endlessids.mixin.helpers.ChunkBiomeHook;
import com.hbm.world.WorldUtil;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = WorldUtil.class, remap = false)
public class WorldUtilMixin {

    /**
     * @author Kellen
     * @reason Update biome ids
     */
    @Overwrite
    public static void setBiome(World world, int x, int z, BiomeGenBase biome){
        int relBlockX = x & 15;
        int relBlockZ = z & 15;
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        ((ChunkBiomeHook) chunk).getBiomeShortArray()[relBlockZ << 4 | relBlockX] = (short)(biome.biomeID);
        chunk.isModified = true;
    }
}
