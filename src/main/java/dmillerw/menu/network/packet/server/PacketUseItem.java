package dmillerw.menu.network.packet.server;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUseItem {
    private int slot;

    public PacketUseItem(int slot) {
        this.slot = slot;
    }

    public static void encode(PacketUseItem pingPacket, PacketBuffer buf) {
        buf.writeInt(pingPacket.slot);
    }

    public static PacketUseItem decode(PacketBuffer buf) {
        return new PacketUseItem(buf.readInt());
    }

    public static class Handler {
        public static void handle(PacketUseItem message, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                ItemStack slotStack = player.inventory.getStackInSlot(message.slot);
                ItemStack heldSaved = player.getHeldItemMainhand();
                Hand hand = Hand.MAIN_HAND;
                EquipmentSlotType slot = getSlotFromHand(hand);

                player.setItemStackToSlot(slot, slotStack);
                ItemStack heldItem = player.getHeldItem(hand);
                ActionResult<ItemStack> useStack = heldItem.useItemRightClick(player.world, player, hand);
                if (useStack.getType() == ActionResultType.SUCCESS) {
                    player.inventory.mainInventory.set(message.slot, useStack.getResult());
                }
                player.setItemStackToSlot(slot, heldSaved);

                player.sendContainerToPlayer(player.container);

                ctx.get().setPacketHandled(true);
            }
        }
    }

    private static EquipmentSlotType getSlotFromHand(Hand hand) {
        return hand == Hand.MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND;
    }
}