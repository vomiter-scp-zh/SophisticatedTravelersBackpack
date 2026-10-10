package com.vomiter.sophtravelerspack.network;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.util.SleepingBagUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;

import java.util.UUID;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL);
        registrar.playToServer(MBPSyncRequest.TYPE, MBPSyncRequest.STREAM_CODEC, MBPSyncRequest::handle);
        registrar.playToClient(MBPSyncResponse.TYPE, MBPSyncResponse.STREAM_CODEC, MBPSyncResponse::handle);
        registrar.playToServer(EuipSBPSleepingBagRequest.TYPE, EuipSBPSleepingBagRequest.STREAM_CODEC,
                EuipSBPSleepingBagRequest::handle);
        registrar.playToServer(InWorldSBPSleepingBagRequest.TYPE, InWorldSBPSleepingBagRequest.STREAM_CODEC,
                InWorldSBPSleepingBagRequest::handle);
    }

    public record MBPSyncResponse(UUID uuid, CompoundTag tag) implements CustomPacketPayload {
        public static final Type<MBPSyncResponse> TYPE = new Type<>(STBackpack.modLoc("backpack_sync_response"));
        public static final StreamCodec<ByteBuf, MBPSyncResponse> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, MBPSyncResponse::uuid,
                ByteBufCodecs.COMPOUND_TAG, MBPSyncResponse::tag,
                MBPSyncResponse::new);

        @Override
        public Type<MBPSyncResponse> type() {
            return TYPE;
        }

        public static void handle(MBPSyncResponse packet, IPayloadContext context) {
            BackpackStorage.get().setBackpackContents(packet.uuid, packet.tag);
            ModNetworkClientHandler.handleMBPSyncResponse(packet);
        }
    }

    public record MBPSyncRequest(UUID uuid) implements CustomPacketPayload {
        public static final Type<MBPSyncRequest> TYPE = new Type<>(STBackpack.modLoc("backpack_sync_request"));
        public static final StreamCodec<ByteBuf, MBPSyncRequest> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, MBPSyncRequest::uuid, MBPSyncRequest::new);

        @Override
        public Type<MBPSyncRequest> type() {
            return TYPE;
        }

        public static void handle(MBPSyncRequest packet, IPayloadContext context) {
            if (!(context.player() instanceof ServerPlayer player)) return;
            PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, slot) -> {
                if (!(backpack.getItem() instanceof BackpackItem)) return false;
                var contentsUuid = BackpackWrapper.fromStack(backpack).getContentsUuid();
                if (contentsUuid.isEmpty() || !contentsUuid.get().equals(packet.uuid)) return false;

                // Validate possession before querying storage; never create contents for an arbitrary UUID.
                BackpackStorage.get().getBackpackContents(packet.uuid).ifPresent(contents ->
                        PacketDistributor.sendToPlayer(player,
                                new MBPSyncResponse(packet.uuid, copyInventoryContents(contents))));
                return true;
            });
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

    public record EuipSBPSleepingBagRequest(int containerId) implements CustomPacketPayload {
        public static final Type<EuipSBPSleepingBagRequest> TYPE =
                new Type<>(STBackpack.modLoc("equipped_sleeping_bag"));
        public static final StreamCodec<ByteBuf, EuipSBPSleepingBagRequest> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, EuipSBPSleepingBagRequest::containerId, EuipSBPSleepingBagRequest::new);

        @Override
        public Type<EuipSBPSleepingBagRequest> type() {
            return TYPE;
        }

        public static void handle(EuipSBPSleepingBagRequest packet, IPayloadContext context) {
            var player = context.player();
            if (player instanceof ServerPlayer && player.containerMenu instanceof BackpackContainer menu
                    && menu.containerId == packet.containerId && menu.stillValid(player)
                    && menu.isFirstLevelStorage()
                    && menu.getBackpackContext().getType() == BackpackContext.ContextType.ITEM_BACKPACK) {
                SleepingBagUtils.useSleepingBagFromItem(player);
            }
        }
    }

    public record InWorldSBPSleepingBagRequest(int containerId, BlockPos pos) implements CustomPacketPayload {
        public static final Type<InWorldSBPSleepingBagRequest> TYPE =
                new Type<>(STBackpack.modLoc("world_sleeping_bag"));
        public static final StreamCodec<FriendlyByteBuf, InWorldSBPSleepingBagRequest> STREAM_CODEC =
                StreamCodec.of(InWorldSBPSleepingBagRequest::encode, InWorldSBPSleepingBagRequest::decode);

        private static void encode(FriendlyByteBuf buffer, InWorldSBPSleepingBagRequest packet) {
            buffer.writeVarInt(packet.containerId);
            buffer.writeBlockPos(packet.pos);
        }

        private static InWorldSBPSleepingBagRequest decode(FriendlyByteBuf buffer) {
            return new InWorldSBPSleepingBagRequest(buffer.readVarInt(), buffer.readBlockPos());
        }

        @Override
        public Type<InWorldSBPSleepingBagRequest> type() {
            return TYPE;
        }

        public static void handle(InWorldSBPSleepingBagRequest packet, IPayloadContext context) {
            var player = context.player();
            if (player instanceof ServerPlayer && player.containerMenu instanceof BackpackContainer menu
                    && menu.containerId == packet.containerId && menu.stillValid(player)
                    && menu.isFirstLevelStorage() && menu.getBlockPosition().filter(packet.pos::equals).isPresent()
                    && player.level().hasChunkAt(packet.pos)
                    && player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) <= 64) {
                if (SleepingBagUtils.isSleepingBagDeployed(menu.getStorageWrapper().getBackpack())) {
                    SleepingBagUtils.recoverSleepingBagIntoSBP(player.level(), packet.pos);
                } else {
                    SleepingBagUtils.deploySleepingBagFromSBP(player.level(), packet.pos);
                }
            }
        }
    }
}
