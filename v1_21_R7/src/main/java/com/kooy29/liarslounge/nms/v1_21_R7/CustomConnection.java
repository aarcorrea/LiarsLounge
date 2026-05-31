package com.kooy29.liarslounge.nms.v1_21_R7;

import com.kooy29.liarslounge.api.nms.CustomConnectionWrapper;
import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import io.netty.channel.*;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
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
    static boolean isSoftwareObfuscated = getClassByName("net.minecraft.server.network.PlayerConnection") != null;

    static Class<?> nmsNetworkManager = getServerClass(isSoftwareObfuscated ? "network.NetworkManager" : "network.Connection");

    static Class<?> serverPacketListener = getServerClass("server.network.ServerCommonPacketListenerImpl");
    static Field serverPacketListenerConn = getField(serverPacketListener, nmsNetworkManager, 0);
    static Class<?> nettyChannel = getNettyClass("channel.Channel");

    static Field paperConnChannel = getField(nmsNetworkManager, nettyChannel, 0);
    static Class<?> paperConnection = getClassByName("io.papermc.paper.connection.PaperCommonConnection");
    static Field paperConnectionHandle = getField(paperConnection, serverPacketListener, 0);
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

    public static void removeChannel(UUID uuid) {
        playerChannelList.remove(uuid);
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

    public static Class<?> getServerClass(String modern) {
        return getClassByName("net.minecraft." + modern);
    }

    public static Class<?> getClassByName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> getNettyClass(String name) {
        return getClassByName("io.netty." + name);
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
                if (packet instanceof PacketPlayInUseEntity packetUseEntity) {
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

                if (msg instanceof net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment packet) {

                    if (!wrapperMethods.isPlayerInArena(player)) {
                        super.write(ctx, msg, promise);
                        return;
                    }

                    List<com.mojang.datafixers.util.Pair<
                            net.minecraft.world.entity.EnumItemSlot,
                            net.minecraft.world.item.ItemStack>> newList = new ArrayList<>();

                    for (var pair : packet.e()) {
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
                            new net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment(
                                    packet.b(), // entityId
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
