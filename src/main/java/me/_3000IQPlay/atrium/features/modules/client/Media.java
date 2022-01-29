package me._3000IQPlay.atrium.features.modules.client;

import me._3000IQPlay.atrium.features.modules.Module;
import me._3000IQPlay.atrium.features.setting.Setting;
import me._3000IQPlay.atrium.util.Util;

public class Media
        extends Module {
    private static Media instance;
    public final Setting<Boolean> changeOwn = this.register(new Setting<Boolean>("MyName", true));
    public final Setting<String> ownName = this.register(new Setting<Object>("Name", "Name here...", v -> this.changeOwn.getValue()));

    public Media() {
        super("Media", "Helps with creating Media", Module.Category.CLIENT, false, false, false);
        instance = this;
    }

    public static Media getInstance() {
        if (instance == null) {
            instance = new Media();
        }
        return instance;
    }

    public static String getPlayerName() {
        if (Media.fullNullCheck() || !PingBypass.getInstance().isConnected()) {
            return Util.mc.getSession().getUsername();
        }
        String name = PingBypass.getInstance().getPlayerName();
        if (name == null || name.isEmpty()) {
            return Util.mc.getSession().getUsername();
        }
        return name;
    }
}

