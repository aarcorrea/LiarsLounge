package com.kooy29.liarslounge.nms.v1_8_R3;

import com.kooy29.liarslounge.api.nms.CustomConnectionWrapper;
import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import io.netty.channel.*;
import net.minecraft.server.v1_8_R3.PacketPlayInSteerVehicle;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class CustomConnection implements CustomConnectionWrapper {
    IWrapperMethods wrapperMethods;

    public CustomConnection(JavaPlugin instance, IWrapperMethods wrapperMethods) {
        this.wrapperMethods = wrapperMethods;
    }

    public void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    public void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                if (packet instanceof PacketPlayInSteerVehicle) {
                    PacketPlayInSteerVehicle packetSteer = (PacketPlayInSteerVehicle) packet;
                    if (packetSteer.d()) {
                        if (!wrapperMethods.canPlayerUnmount(player)) {
                            return;
                        }
                    }
                } else if (packet instanceof PacketPlayInUseEntity) {
                    PacketPlayInUseEntity packetUseEntity = (PacketPlayInUseEntity) packet;
                    try {
                        int entityId = getPrivateField(packetUseEntity, "a");
                        if (IWrapperMethods.armorStands.contains(entityId)) {
                            Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEntityEvent(player, null));
                            return;
                        }
                    } catch (Exception e) {
                        // ignored
                    }
                }
                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof PacketPlayOutEntityEquipment) {
                    net.minecraft.server.v1_8_R3.ItemStack itemStack = getPrivateField(msg, "c");
                    if (itemStack != null && wrapperMethods.isPlayerInArena(player)) {
                        PacketPlayOutEntityEquipment packet = (PacketPlayOutEntityEquipment) msg;
                        if (VersionWrapper.getCustomDataNMS(itemStack).startsWith("CARD_")) {
                            setPrivateFieldQuestionCard(packet, "c");
                        }
                        super.write(ctx, packet, promise);
                        return;
                    }
                }
                super.write(ctx, msg, promise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
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

    public void setPrivateFieldQuestionCard(Object packet, String fieldName) {
        try {
            Field field = packet.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(packet, CraftItemStack.asNMSCopy(wrapperMethods.getHiddenCardItem()));
        } catch (Exception e) {
            // ignored
        }
    }
}
