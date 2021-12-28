package me._3000IQPlay.atrium.features.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import me._3000IQPlay.atrium.event.events.PacketEvent;
import me._3000IQPlay.atrium.event.events.UpdateWalkingPlayerEvent;
import me._3000IQPlay.atrium.features.modules.Module;
import me._3000IQPlay.atrium.features.setting.Setting;
import me._3000IQPlay.atrium.util.BlockUtil;
import me._3000IQPlay.atrium.util.DamageUtil;
import me._3000IQPlay.atrium.util.EntityUtil;
import me._3000IQPlay.atrium.util.InventoryUtil;
import me._3000IQPlay.atrium.util.MathUtil;
import me._3000IQPlay.atrium.util.RotationUtil;
import me._3000IQPlay.atrium.util.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BedAura
        extends Module {
    private final Setting<Boolean> place = this.register(new Setting<Boolean>("Place", false));
    private final Setting<Integer> placeDelay = this.register(new Setting<Object>("Placedelay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(500), v -> this.place.getValue()));
    private final Setting<Float> placeRange = this.register(new Setting<Object>("PlaceRange", Float.valueOf(6.0f), Float.valueOf(1.0f), Float.valueOf(10.0f), v -> this.place.getValue()));
    private final Setting<Boolean> extraPacket = this.register(new Setting<Object>("InsanePacket", Boolean.valueOf(false), v -> this.place.getValue()));
    private final Setting<Boolean> packet = this.register(new Setting<Object>("Packet", Boolean.valueOf(false), v -> this.place.getValue()));
    private final Setting<Boolean> explode = this.register(new Setting<Boolean>("Break", true));
    private final Setting<Integer> breakDelay = this.register(new Setting<Object>("Breakdelay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(500), v -> this.explode.getValue()));
    private final Setting<Float> breakRange = this.register(new Setting<Object>("BreakRange", Float.valueOf(6.0f), Float.valueOf(1.0f), Float.valueOf(10.0f), v -> this.explode.getValue()));
    private final Setting<Float> minDamage = this.register(new Setting<Object>("MinDamage", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(36.0f), v -> this.explode.getValue()));
    private final Setting<Float> range = this.register(new Setting<Object>("Range", Float.valueOf(10.0f), Float.valueOf(1.0f), Float.valueOf(12.0f), v -> this.explode.getValue()));
    private final Setting<Boolean> suicide = this.register(new Setting<Object>("Suicide", Boolean.valueOf(false), v -> this.explode.getValue()));
    private final Setting<Boolean> removeTiles = this.register(new Setting<Boolean>("RemoveTiles", false));
    private final Setting<Boolean> rotate = this.register(new Setting<Boolean>("Rotate", false));
    private final Setting<Logic> logic = this.register(new Setting<Object>("Logic", (Object)Logic.BREAKPLACE, v -> this.place.getValue() != false && this.explode.getValue() != false));
    private final Timer breakTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private EntityPlayer target = null;
    private boolean sendRotationPacket = false;
    private final AtomicDouble yaw = new AtomicDouble(-1.0);
    private final AtomicDouble pitch = new AtomicDouble(-1.0);
    private final AtomicBoolean shouldRotate = new AtomicBoolean(false);
    private BlockPos maxPos = null;
    private int lastHotbarSlot = -1;
    private int bedSlot = -1;

    public BedAura() {
        super("BedAura", "AutoPlace and Break for beds", Module.Category.COMBAT, true, false, false);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event) {
        if (this.shouldRotate.get() && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            packet.yaw = (float)this.yaw.get();
            packet.pitch = (float)this.pitch.get();
            this.shouldRotate.set(false);
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getStage() != 0 || BedAura.fullNullCheck() || BedAura.mc.player.dimension != -1 && BedAura.mc.player.dimension != 1) {
            return;
        }
        this.doBedAura();
    }

    private void doBedAura() {
        switch (this.logic.getValue()) {
            case BREAKPLACE: {
                this.mapBeds();
                this.breakBeds();
                this.placeBeds();
                break;
            }
            case PLACEBREAK: {
                this.mapBeds();
                this.placeBeds();
                this.breakBeds();
            }
        }
    }

    private void breakBeds() {
        if (this.explode.getValue().booleanValue() && this.breakTimer.passedMs(this.breakDelay.getValue().intValue()) && this.maxPos != null) {
            BedAura.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity) BedAura.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            BlockUtil.rightClickBlockLegit(this.maxPos, this.range.getValue().floatValue(), this.rotate.getValue() != false && this.place.getValue() == false, EnumHand.MAIN_HAND, this.yaw, this.pitch, this.shouldRotate, true);
            if (BedAura.mc.player.isSneaking()) {
                BedAura.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity) BedAura.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            this.breakTimer.reset();
        }
    }

    private void mapBeds() {
        this.maxPos = null;
        float maxDamage = 0.5f;
        if (this.removeTiles.getValue().booleanValue()) {
            ArrayList<BedData> removedBlocks = new ArrayList<BedData>();
            for (TileEntity tile : BedAura.mc.world.loadedTileEntityList) {
                if (!(tile instanceof TileEntityBed)) continue;
                TileEntityBed bed = (TileEntityBed)tile;
                BedData data = new BedData(tile.getPos(), BedAura.mc.world.getBlockState(tile.getPos()), bed, bed.isHeadPiece());
                removedBlocks.add(data);
            }
            for (BedData data : removedBlocks) {
                BedAura.mc.world.setBlockToAir(data.getPos());
            }
            for (BedData data : removedBlocks) {
                float selfDamage;
                BlockPos pos;
                if (!data.isHeadPiece() || !(BedAura.mc.player.getDistanceSq(pos = data.getPos()) <= MathUtil.square(this.breakRange.getValue().floatValue())) || !((double)(selfDamage = DamageUtil.calculateDamage(pos, (Entity) BedAura.mc.player)) + 1.0 < (double)EntityUtil.getHealth((Entity) BedAura.mc.player)) && DamageUtil.canTakeDamage(this.suicide.getValue())) continue;
                for (EntityPlayer player : BedAura.mc.world.playerEntities) {
                    float damage;
                    if (!(player.getDistanceSq(pos) < MathUtil.square(this.range.getValue().floatValue())) || !EntityUtil.isValid((Entity)player, this.range.getValue().floatValue() + this.breakRange.getValue().floatValue()) || !((damage = DamageUtil.calculateDamage(pos, (Entity)player)) > selfDamage || damage > this.minDamage.getValue().floatValue() && !DamageUtil.canTakeDamage(this.suicide.getValue())) && !(damage > EntityUtil.getHealth((Entity)player)) || !(damage > maxDamage)) continue;
                    maxDamage = damage;
                    this.maxPos = pos;
                }
            }
            for (BedData data : removedBlocks) {
                BedAura.mc.world.setBlockState(data.getPos(), data.getState());
            }
        } else {
            for (TileEntity tile : BedAura.mc.world.loadedTileEntityList) {
                float selfDamage;
                BlockPos pos;
                TileEntityBed bed;
                if (!(tile instanceof TileEntityBed) || !(bed = (TileEntityBed)tile).isHeadPiece() || !(BedAura.mc.player.getDistanceSq(pos = bed.getPos()) <= MathUtil.square(this.breakRange.getValue().floatValue())) || !((double)(selfDamage = DamageUtil.calculateDamage(pos, (Entity) BedAura.mc.player)) + 1.0 < (double)EntityUtil.getHealth((Entity) BedAura.mc.player)) && DamageUtil.canTakeDamage(this.suicide.getValue())) continue;
                for (EntityPlayer player : BedAura.mc.world.playerEntities) {
                    float damage;
                    if (!(player.getDistanceSq(pos) < MathUtil.square(this.range.getValue().floatValue())) || !EntityUtil.isValid((Entity)player, this.range.getValue().floatValue() + this.breakRange.getValue().floatValue()) || !((damage = DamageUtil.calculateDamage(pos, (Entity)player)) > selfDamage || damage > this.minDamage.getValue().floatValue() && !DamageUtil.canTakeDamage(this.suicide.getValue())) && !(damage > EntityUtil.getHealth((Entity)player)) || !(damage > maxDamage)) continue;
                    maxDamage = damage;
                    this.maxPos = pos;
                }
            }
        }
    }

    private void placeBeds() {
        if (this.place.getValue().booleanValue() && this.placeTimer.passedMs(this.placeDelay.getValue().intValue()) && this.maxPos == null) {
            this.bedSlot = this.findBedSlot();
            if (this.bedSlot == -1) {
                if (BedAura.mc.player.getHeldItemOffhand().getItem() == Items.BED) {
                    this.bedSlot = -2;
                } else {
                    return;
                }
            }
            this.lastHotbarSlot = BedAura.mc.player.inventory.currentItem;
            this.target = EntityUtil.getClosestEnemy(this.placeRange.getValue().floatValue());
            if (this.target != null) {
                BlockPos targetPos = new BlockPos(this.target.getPositionVector());
                this.placeBed(targetPos, true);
            }
        }
    }

    private void placeBed(BlockPos pos, boolean firstCheck) {
        if (BedAura.mc.world.getBlockState(pos).getBlock() == Blocks.BED) {
            return;
        }
        float damage = DamageUtil.calculateDamage(pos, (Entity) BedAura.mc.player);
        if ((double)damage > (double)EntityUtil.getHealth((Entity) BedAura.mc.player) + 0.5) {
            if (firstCheck) {
                this.placeBed(pos.up(), false);
            }
            return;
        }
        if (!BedAura.mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            if (firstCheck) {
                this.placeBed(pos.up(), false);
            }
            return;
        }
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        HashMap<BlockPos, EnumFacing> facings = new HashMap<BlockPos, EnumFacing>();
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos position;
            if (facing == EnumFacing.DOWN || facing == EnumFacing.UP || !(BedAura.mc.player.getDistanceSq(position = pos.offset(facing)) <= MathUtil.square(this.placeRange.getValue().floatValue())) || !BedAura.mc.world.getBlockState(position).getMaterial().isReplaceable() || BedAura.mc.world.getBlockState(position.down()).getMaterial().isReplaceable()) continue;
            positions.add(position);
            facings.put(position, facing.getOpposite());
        }
        if (positions.isEmpty()) {
            if (firstCheck) {
                this.placeBed(pos.up(), false);
            }
            return;
        }
        positions.sort(Comparator.comparingDouble(pos2 -> BedAura.mc.player.getDistanceSq(pos2)));
        BlockPos finalPos = (BlockPos)positions.get(0);
        EnumFacing finalFacing = (EnumFacing)facings.get((Object)finalPos);
        float[] rotation = RotationUtil.simpleFacing(finalFacing);
        if (!this.sendRotationPacket && this.extraPacket.getValue().booleanValue()) {
            RotationUtil.faceYawAndPitch(rotation[0], rotation[1]);
            this.sendRotationPacket = true;
        }
        this.yaw.set((double)rotation[0]);
        this.pitch.set((double)rotation[1]);
        this.shouldRotate.set(true);
        Vec3d hitVec = new Vec3d((Vec3i)finalPos.down()).add(0.5, 0.5, 0.5).add(new Vec3d(finalFacing.getOpposite().getDirectionVec()).scale(0.5));
        BedAura.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity) BedAura.mc.player, CPacketEntityAction.Action.START_SNEAKING));
        InventoryUtil.switchToHotbarSlot(this.bedSlot, false);
        BlockUtil.rightClickBlock(finalPos.down(), hitVec, this.bedSlot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, EnumFacing.UP, this.packet.getValue());
        BedAura.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity) BedAura.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        this.placeTimer.reset();
    }

    @Override
    public String getDisplayInfo() {
        if (this.target != null) {
            return this.target.getName();
        }
        return null;
    }

    @Override
    public void onToggle() {
        this.lastHotbarSlot = -1;
        this.bedSlot = -1;
        this.sendRotationPacket = false;
        this.target = null;
        this.yaw.set(-1.0);
        this.pitch.set(-1.0);
        this.shouldRotate.set(false);
    }

    private int findBedSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = BedAura.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || stack.getItem() != Items.BED) continue;
            return i;
        }
        return -1;
    }

    public static enum Logic {
        BREAKPLACE,
        PLACEBREAK;

    }

    public static class BedData {
        private final BlockPos pos;
        private final IBlockState state;
        private final boolean isHeadPiece;
        private final TileEntityBed entity;

        public BedData(BlockPos pos, IBlockState state, TileEntityBed bed, boolean isHeadPiece) {
            this.pos = pos;
            this.state = state;
            this.entity = bed;
            this.isHeadPiece = isHeadPiece;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public IBlockState getState() {
            return this.state;
        }

        public boolean isHeadPiece() {
            return this.isHeadPiece;
        }

        public TileEntityBed getEntity() {
            return this.entity;
        }
    }
}