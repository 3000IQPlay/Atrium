package me._3000IQPlay.atrium.features.modules.misc;

import me._3000IQPlay.atrium.features.setting.Setting;
import me._3000IQPlay.atrium.features.modules.Module;

public class Suicide
        extends Module {
    public Suicide() {
        super("Suicide", "Auto suicide.", Module.Category.MISC, true, false, false);
    }

    @Override
    public void onEnable() {
        mc.player.sendChatMessage("/kill");
        toggle();
    }
}