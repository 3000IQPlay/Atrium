package me._3000IQPlay.atrium.features.modules.client;

import me._3000IQPlay.atrium.features.modules.Module;
import me._3000IQPlay.atrium.features.setting.Setting;

public class ModuleTools extends Module {

    private static ModuleTools INSTANCE;

    public Setting<Notifier> notifier = register(new Setting("ModuleNotifier", Notifier.ATRIUM));
    public Setting<PopNotifier> popNotifier = register(new Setting("PopNotifier", PopNotifier.ATRIUM));

    public ModuleTools() {
        super("ModuleTools", "Change settings", Category.CLIENT, true, false, false);
        INSTANCE = this;
    }


    public static ModuleTools getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModuleTools();
        }
        return INSTANCE;
    }


    public enum Notifier{
        ATRIUM,
        FUTURE,
        DOTGOD;
    }

    public enum PopNotifier{
        ATRIUM,
        FUTURE,
        DOTGOD,
        NONE
    }


}
