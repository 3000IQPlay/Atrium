package me._3000IQPlay.atrium.features.modules.misc;

import me._3000IQPlay.atrium.features.modules.Module;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class NoEntityTrace extends Module {

    private boolean focus = false;

    public NoEntityTrace() {
        super("NoEntityTrace", "Mines trought entities", Module.Category.MISC, false, false, false);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .filter(entity -> mc.player == entity)
                .map(entity -> (EntityLivingBase) entity)
                .filter(entity -> !(entity.isDead))
                .forEach(this::process);

        RayTraceResult normalResult = mc.objectMouseOver;

        if (normalResult != null) {
            focus = normalResult.typeOfHit == RayTraceResult.Type.ENTITY;
        }
    }

    private void process(EntityLivingBase event) {
        RayTraceResult bypassEntityResult = event.rayTrace(6, mc.getRenderPartialTicks());

        if (bypassEntityResult != null && focus) {
            if (bypassEntityResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos pos = bypassEntityResult.getBlockPos();

                if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                    mc.playerController.onPlayerDamageBlock(pos, EnumFacing.UP);
                }
            }
        }
    }

}
