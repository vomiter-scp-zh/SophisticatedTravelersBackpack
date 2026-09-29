package com.vomiter.sophtravelerspack.network;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.util.SleepingBagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CHANNEL = NetworkRegistry.newSimpleChannel(
                    STBackpack.modLoc("main"),
                    () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
            );

            int id = 0;
            CHANNEL.messageBuilder(MBPSyncRequest.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(MBPSyncRequest::encode)
                    .decoder(MBPSyncRequest::decode)
                    .consumerMainThread(MBPSyncRequest::handle)
                    .add();
            CHANNEL.messageBuilder(MBPSyncResponse.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(MBPSyncResponse::encode)
                    .decoder(MBPSyncResponse::decode)
                    .consumerMainThread(MBPSyncResponse::handle)
                    .add();
            CHANNEL.messageBuilder(EuipSBPSleepingBagRequest.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(EuipSBPSleepingBagRequest::encode)
                    .decoder(EuipSBPSleepingBagRequest::decode)
                    .consumerMainThread(EuipSBPSleepingBagRequest::handle)
                    .add();
            CHANNEL.messageBuilder(InWorldSBPSleepingBagRequest.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(InWorldSBPSleepingBagRequest::encode)
                    .decoder(InWorldSBPSleepingBagRequest::decode)
                    .consumerMainThread(InWorldSBPSleepingBagRequest::handle)
                    .add();

        });

    }

    public record MBPSyncResponse(UUID uuid, CompoundTag tag){
        public static void encode(MBPSyncResponse pkt, FriendlyByteBuf buf){
            buf.writeUUID(pkt.uuid);
            buf.writeNbt(pkt.tag);
        }

        public static MBPSyncResponse decode(FriendlyByteBuf buf){
            return new MBPSyncResponse(buf.readUUID(), buf.readNbt());
        }

        public static void handle(MBPSyncResponse pkt, Supplier<NetworkEvent.Context> ctx) {
            BackpackStorage.get().setBackpackContents(pkt.uuid, pkt.tag);
            ModNetworkClientHandler.handleMBPSyncResponse(pkt);
            ctx.get().setPacketHandled(true);

        }
    }

    public record MBPSyncRequest(UUID uuid){
        public static void encode(MBPSyncRequest pkt, FriendlyByteBuf buf) {
            buf.writeUUID(pkt.uuid);
        }
        public static MBPSyncRequest decode(FriendlyByteBuf buf) {
            return new MBPSyncRequest(buf.readUUID());
        }
        public static void handle(MBPSyncRequest pkt, Supplier<NetworkEvent.Context> ctx) {
            var sourceContents = BackpackStorage.get()
                    .getOrCreateBackpackContents(
                            pkt.uuid
                    );

            CompoundTag responseContents =
                    copyInventoryContents(sourceContents);

            PlayerInventoryProvider.get().runOnBackpacks(Objects.requireNonNull(ctx.get().getSender()), ((backpack, s, s1, i) -> {
                backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent(iBackpackWrapper -> {
                    iBackpackWrapper.getContentsUuid().ifPresent(uuid1 -> {
                        if (uuid1.equals(pkt.uuid)){
                            CHANNEL.send(
                                    PacketDistributor.PLAYER.with(() -> ctx.get().getSender()),
                                    new MBPSyncResponse(uuid1, responseContents)
                            );
                        }
                    });
                });
                return false;
            }));

            ctx.get().setPacketHandled(true);
        }

        private static CompoundTag copyInventoryContents(
                CompoundTag source
        ) {
            CompoundTag result = new CompoundTag();
            result.put(
                    "inventory",
                    source.contains("inventory", Tag.TAG_COMPOUND)
                            ? source.getCompound("inventory").copy()
                            : new CompoundTag()
            );

            result.put(
                    "upgradeInventory",
                    source.contains("upgradeInventory", Tag.TAG_COMPOUND)
                            ? source.getCompound("upgradeInventory").copy()
                            : new CompoundTag()
            );

            return result;
        }
    }

    public record EuipSBPSleepingBagRequest(int containerId) {
        public static void encode(EuipSBPSleepingBagRequest packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.containerId);
        }

        public static EuipSBPSleepingBagRequest decode(FriendlyByteBuf buffer) {
            return new EuipSBPSleepingBagRequest(buffer.readVarInt());
        }

        public static void handle(EuipSBPSleepingBagRequest packet, Supplier<NetworkEvent.Context> context) {
            var player = context.get().getSender();
            if (player != null && player.containerMenu instanceof BackpackContainer menu
                    && menu.containerId == packet.containerId && menu.stillValid(player)
                    && menu.isFirstLevelStorage()
                    && menu.getBackpackContext().getType() == BackpackContext.ContextType.ITEM_BACKPACK) {
                SleepingBagUtils.useSleepingBagFromItem(player);
            }
            context.get().setPacketHandled(true);
        }
    }

    public record InWorldSBPSleepingBagRequest(int containerId, BlockPos pos) {
        public static void encode(InWorldSBPSleepingBagRequest packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.containerId);
            buffer.writeBlockPos(packet.pos);
        }

        public static InWorldSBPSleepingBagRequest decode(FriendlyByteBuf buffer) {
            return new InWorldSBPSleepingBagRequest(buffer.readVarInt(), buffer.readBlockPos());
        }

        public static void handle(InWorldSBPSleepingBagRequest packet, Supplier<NetworkEvent.Context> context) {
            var player = context.get().getSender();
            if (player != null && player.containerMenu instanceof BackpackContainer menu
                    && menu.containerId == packet.containerId && menu.stillValid(player)
                    && menu.isFirstLevelStorage() && menu.getBlockPosition().filter(packet.pos::equals).isPresent()
                    && player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) <= 64) {
                if (SleepingBagUtils.isSleepingBagDeployed(menu.getStorageWrapper().getBackpack())) {
                    SleepingBagUtils.recoverSleepingBagIntoSBP(player.level(), packet.pos);
                } else {
                    SleepingBagUtils.deploySleepingBagFromSBP(player.level(), packet.pos);
                }
            }
            context.get().setPacketHandled(true);
        }
    }
}
