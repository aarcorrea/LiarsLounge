package com.kooy29.liarslounge.nms.paper;

import com.kooy29.liarslounge.api.nms.CustomConnectionWrapper;
import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import io.netty.channel.*;
import io.papermc.paper.connection.PaperCommonConnection;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomConnection implements CustomConnectionWrapper {
    static Map<UUID, Channel> playerChannelList = new ConcurrentHashMap<>();

    static Field serverPacketListenerConn = getField(ServerCommonPacketListenerImpl.class, Connection.class, 0);
    static Field paperConnChannel = getField(Connection.class, Channel.class, 0);
    static Field paperConnectionHandle = getField(PaperCommonConnection.class, ServerCommonPacketListenerImpl.class, 0);

    JavaPlugin instance;
    IWrapperMethods wrapperMethods;

    public CustomConnection(JavaPlugin instance, IWrapperMethods wrapperMethods) {
        this.instance = instance;
        this.wrapperMethods = wrapperMethods;
    }

    public static Field getField(Class<?> target, Class<?> dataType, int index) {
        if (target == null || dataType == null) return null;

        int current = 0;
        for (Field field : getFields(target)) {
            if (dataType.isAssignableFrom(field.getType())) {
                if (current++ == index) return field;
            }
        }

        if (target.getSuperclass() != null) {
            return getField(target.getSuperclass(), dataType, index);
        }

        return null;
    }

    public static Field[] getFields(Class<?> target) {
        if (target == null) return new Field[0];

        Field[] declaredFields = target.getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                field.setAccessible(true);
            } catch (Throwable ignored) {
            }
        }
        return declaredFields;
    }

    public static void setChannel(Object connection, UUID uuid) {
        Channel channel = (Channel) getChannelFromPaperConnection(connection);
        playerChannelList.put(uuid, channel);
    }

    public static Object getChannelFromPaperConnection(Object paperConnection) {
        try {
            Object packetListener = paperConnectionHandle.get(paperConnection);
            Object connection = serverPacketListenerConn.get(packetListener);
            return paperConnChannel.get(connection);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void removePlayer(Player player) {
        Channel channel = playerChannelList.remove(player.getUniqueId());
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    public void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                if (packet instanceof ServerboundInteractPacket packetUseEntity) {
                    try {
                        int entityId = getPrivateField(packetUseEntity, "b");
                        if (IWrapperMethods.armorStands.contains(entityId)) {
                            Bukkit.getScheduler().runTask(instance, () -> Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEntityEvent(player, null)));
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

                if (msg instanceof ClientboundSetEquipmentPacket packet) {

                    if (!wrapperMethods.isPlayerInArena(player)) {
                        super.write(ctx, msg, promise);
                        return;
                    }

                    List<com.mojang.datafixers.util.Pair<
                            net.minecraft.world.entity.EquipmentSlot,
                            net.minecraft.world.item.ItemStack>> newList = new ArrayList<>();

                    for (var pair : packet.getSlots()) {
                        var slot = pair.getFirst();
                        net.minecraft.world.item.ItemStack item = pair.getSecond();

                        if (item != null) {
                            org.bukkit.inventory.ItemStack bukkitItem =
                                    CraftItemStack.asBukkitCopy(item);

                            String data = VersionWrapper.getCustomDataNMS(bukkitItem);

                            if (data.startsWith("CARD_")) {
                                item = setQuestionCard(); // return NEW ItemStack
                            }
                        }

                        newList.add(com.mojang.datafixers.util.Pair.of(slot, item));
                    }

                    // create a NEW packet
                    var newPacket =
                            new net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket(
                                    packet.getEntity(),
                                    newList
                            );

                    super.write(ctx, newPacket, promise);
                    return;
                }

                super.write(ctx, msg, promise);
            }

        };

        ChannelPipeline pipeline = playerChannelList.get(player.getUniqueId()).pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPrivateField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public net.minecraft.world.item.ItemStack setQuestionCard() {
        org.bukkit.inventory.ItemStack hidden =
                wrapperMethods.getHiddenCardItem();

        return CraftItemStack.asNMSCopy(hidden);
    }
}
