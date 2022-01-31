package me._3000IQPlay.atrium.features.modules.combat;

import me._3000IQPlay.atrium.util.BurrowUtil;
import me._3000IQPlay.atrium.features.command.Command;
import me._3000IQPlay.atrium.features.setting.Setting;
import me._3000IQPlay.atrium.features.modules.Module;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class Burrow
        extends Module {
    private final Setting<Integer> offset = this.register(new Setting<Integer>("Offset", 3, -5, 5));
    private final Setting<Boolean> ground = this.register(new Setting<Boolean>("Ground check", true));
    private final Setting<Boolean> rotate = this.register(new Setting<Boolean>("Rotate", false));
    private final Setting<Boolean> echest = this.register(new Setting<Boolean>("Use echest", false));
    private final Setting<Boolean> anvil = this.register(new Setting<Boolean>("Use anvil", false));
    private BlockPos originalPos;
    private int oldSlot = -1;

    public Burrow() {
        super("Burrow", "TPs you into a block", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.originalPos = new BlockPos(Burrow.mc.player.posX, Burrow.mc.player.posY, Burrow.mc.player.posZ);
        if (Burrow.mc.world.getBlockState(new BlockPos(Burrow.mc.player.posX, Burrow.mc.player.posY, Burrow.mc.player.posZ)).getBlock().equals(Blocks.OBSIDIAN) || this.intersectsWithEntity(this.originalPos)) {
            this.toggle();
            return;
        }
        this.oldSlot = Burrow.mc.player.inventory.currentItem;
    }

    @Override
    public void onUpdate() {
        if (this.ground.getValue().booleanValue() && !Burrow.mc.player.onGround) {
            this.toggle();
            return;
        }
        if (this.anvil.getValue().booleanValue() && BurrowUtil.findHotbarBlock(BlockAnvil.class) != -1) {
            BurrowUtil.switchToSlot(BurrowUtil.findHotbarBlock(BlockAnvil.class));
        } else if (this.echest.getValue() != false ? BurrowUtil.findHotbarBlock(BlockEnderChest.class) != -1 : BurrowUtil.findHotbarBlock(BlockObsidian.class) != -1) {
            BurrowUtil.switchToSlot(this.echest.getValue() != false ? BurrowUtil.findHotbarBlock(BlockEnderChest.class) : BurrowUtil.findHotbarBlock(BlockObsidian.class));
        } else {
            Command.sendMessage("Unable to place burrow block (anvil, ec or oby)");
            this.toggle();
            return;
        }
        Burrow.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 0.41999998688698, Burrow.mc.player.posZ, true));
        Burrow.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 0.7531999805211997, Burrow.mc.player.posZ, true));
        Burrow.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 1.00133597911214, Burrow.mc.player.posZ, true));
        Burrow.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 1.16610926093821, Burrow.mc.player.posZ, true));
        BurrowUtil.placeBlock(this.originalPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, false);
        Burrow.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + (double) this.offset.getValue().intValue(), Burrow.mc.player.posZ, false));
        Burrow.mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) Burrow.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        Burrow.mc.player.setSneaking(false);
        BurrowUtil.switchToSlot(this.oldSlot);
        this.toggle();
    }

    private boolean intersectsWithEntity(BlockPos pos) {
        for (Entity entity : Burrow.mc.world.loadedEntityList) {
            if (entity.equals((Object) Burrow.mc.player) || entity instanceof EntityItem || !new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox()))
                continue;
            return true;
        }
        return false;
    }
}

