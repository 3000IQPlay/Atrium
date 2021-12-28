package me._3000IQPlay.atrium.features.modules.movement;

import me._3000IQPlay.atrium.features.modules.Module;

public class ReverseStep
        extends Module {
    public ReverseStep() {
        super("ReverseStep", "Screams chinese words and teleports you", Module.Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if (ReverseStep.mc.player.onGround) {
            ReverseStep.mc.player.motionY -= 1.0;
        }
    }
}

