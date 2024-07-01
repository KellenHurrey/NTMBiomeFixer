package com.kellen.ntmbiomefixer.packet;

import com.kellen.ntmbiomefixer.NTMBiomeFixer;
import com.kellen.ntmbiomefixer.Tags;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketDispatcher {

    public static final SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

    public static void registerPackets(){
        int i = 0;
        wrapper.registerMessage(ChangedBiomeSyncPacket.Handler.class, ChangedBiomeSyncPacket.class, i++, Side.CLIENT);
    }
}
