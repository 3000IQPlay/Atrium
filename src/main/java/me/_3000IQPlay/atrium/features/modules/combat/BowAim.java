package me._3000IQPlay.atrium.features.modules.combat;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.util.EntityUtil;
import me._3000IQPlay.atrium.util.MathUtil;
import me._3000IQPlay.atrium.features.modules.Module;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.math.Vec3d;

public class BowAim
        extends Module {
    public BowAim() {
        super("BowAim", "BowAim", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if (BowAim.mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && BowAim.mc.player.isHandActive() && BowAim.mc.player.getItemInUseMaxCount() >= 3) {
            EntityPlayer player = null;
            float tickDis = 100.0f;
            for (EntityPlayer p : BowAim.mc.world.playerEntities) {
                float dis;
                if (p instanceof EntityPlayerSP || Atrium.friendManager.isFriend(p.getName()) || !((dis = p.getDistance((Entity) BowAim.mc.player)) < tickDis))
                    continue;
                tickDis = dis;
                player = p;
            }
            if (player != null) {
                Vec3d pos = EntityUtil.getInterpolatedPos(player, mc.getRenderPartialTicks());
                float[] angels = MathUtil.calcAngle(EntityUtil.getInterpolatedPos((Entity) BowAim.mc.player, mc.getRenderPartialTicks()), pos);
                BowAim.mc.player.rotationYaw = angels[0];
                BowAim.mc.player.rotationPitch = angels[1];
            }
        }
    }
}