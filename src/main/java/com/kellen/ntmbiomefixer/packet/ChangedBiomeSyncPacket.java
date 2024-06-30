package com.kellen.ntmbiomefixer.packet;

import com.falsepattern.endlessids.mixin.helpers.ChunkBiomeHook;
import com.kellen.ntmbiomefixer.NTMBiomeFixer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChangedBiomeSyncPacket implements IMessage {
    int chunkX;
    int chunkZ;
    byte blockX;
    byte blockZ;
    short biome;
    short[] biomeArray;

    public ChangedBiomeSyncPacket() { }

    public ChangedBiomeSyncPacket(int chunkX, int chunkZ, short[] biomeArray) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.biomeArray = biomeArray;
    }

    public ChangedBiomeSyncPacket(int blockX, int blockZ, short biome) {
        this.chunkX = blockX >> 4;
        this.chunkZ = blockZ >> 4;
        this.blockX = (byte) (blockX & 15);
        this.blockZ = (byte) (blockZ & 15);
        this.biome = biome;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.chunkX);
        buf.writeInt(this.chunkZ);

        if(this.biomeArray == null) {
            buf.writeBoolean(false);
            buf.writeShort(this.biome);
            buf.writeByte(this.blockX);
            buf.writeByte(this.blockZ);
        } else {
            buf.writeBoolean(true);
            for(int i = 0; i < 256; i++) {
                buf.writeShort(this.biomeArray[i]);
            }
        }

        NTMBiomeFixer.debug("toBytes");
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();

        if(!buf.readBoolean()) {
            this.biome = buf.readShort();
            this.blockX = buf.readByte();
            this.blockZ = buf.readByte();
        } else {
            this.biomeArray = new short[256];
            for(int i = 0; i < 256; i++) {
                this.biomeArray[i] = buf.readShort();
            }
        }

        NTMBiomeFixer.debug("fromBytes");
    }

    public static class Handler implements IMessageHandler<ChangedBiomeSyncPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ChangedBiomeSyncPacket m, MessageContext ctx) {
            NTMBiomeFixer.debug("onMessage1");
            World world = Minecraft.getMinecraft().theWorld;
            if(!world.getChunkProvider().chunkExists(m.chunkX, m.chunkZ)) return null;
            Chunk chunk = world.getChunkFromChunkCoords(m.chunkX, m.chunkZ);
            chunk.isModified = true;
            NTMBiomeFixer.debug("onMessage2");
            if(m.biomeArray == null) {
                ((ChunkBiomeHook) chunk).getBiomeShortArray()[(m.blockZ & 15) << 4 | (m.blockX & 15)] = m.biome;
                world.markBlockRangeForRenderUpdate(m.chunkX << 4, 0, m.chunkZ << 4, m.chunkX << 4, 255, m.chunkZ << 4);
            } else {
                for(int i = 0; i < 256; i++) {
                    ((ChunkBiomeHook) chunk).getBiomeShortArray()[i] = m.biomeArray[i];
                    world.markBlockRangeForRenderUpdate(m.chunkX << 4, 0, m.chunkZ << 4, (m.chunkX << 4) + 15, 255, (m.chunkZ << 4) + 15);
                }
            }
            NTMBiomeFixer.debug("onMessage");
            return null;
        }
    }
}
