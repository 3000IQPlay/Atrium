package me._3000IQPlay.atrium.features.gui.custom.loader.hwid;

import me._3000IQPlay.atrium.features.gui.custom.loader.hwid.manager.HWIDManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HWIDAuth.MODID, name = HWIDAuth.NAME, version = HWIDAuth.VERSION)
public class HWIDAuth {

    public static final String MODID = "hwidauthmod";
    public static final String NAME = "HWIDVerification";
    public static final String VERSION = "1.0.0";

    public static String getVersion() {
        return VERSION;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        HWIDManager.hwidCheck(); // Does the HWID check.
    }
}
