package me._3000IQPlay.atrium.mixin;

import me._3000IQPlay.atrium.Atrium;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class AtriumMixinLoader
implements IFMLLoadingPlugin {
    private static boolean isObfuscatedEnvironment = false;

    public AtriumMixinLoader() {
        Atrium.LOGGER.info("Atrium mixins initialized");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.atrium.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        Atrium.LOGGER.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
        isObfuscatedEnvironment = (Boolean)data.get("runtimeDeobfuscationEnabled");
    }

    public String getAccessTransformerClass() {
        return null;
    }
}

