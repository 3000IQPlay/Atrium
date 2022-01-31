package me._3000IQPlay.atrium.features.modules.client;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.event.events.ClientEvent;
import me._3000IQPlay.atrium.features.command.Command;
import me._3000IQPlay.atrium.features.gui.AtriumGui;
import me._3000IQPlay.atrium.features.modules.Module;
import me._3000IQPlay.atrium.features.setting.Setting;
import me._3000IQPlay.atrium.util.Util;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClickGui
        extends Module {
    private static ClickGui INSTANCE = new ClickGui();
    public Setting<Boolean> colorSync = this.register(new Setting<Boolean>("Sync", false));
    public Setting<Boolean> outline = this.register(new Setting<Boolean>("Outline", true));
    public Setting<Boolean> snowing = this.register(new Setting<Boolean>("Snowing", true));
    public Setting<Boolean> moduleDescription = this.register(new Setting<Boolean>("Description", true));
    public Setting<Boolean> blurEffect = this.register(new Setting<Boolean>("Blur", false));
    public Setting<Boolean> scroll = this.register(new Setting<Boolean>("Scroll", true));
    public Setting<Integer> scrollval = this.register(new Setting<Integer>("Scroll Speed", 10, 1, 30, v -> this.scroll.getValue()));
    public Setting<Boolean> rainbowRolling = this.register(new Setting<Object>("RollingRainbow", Boolean.valueOf(false), v -> this.colorSync.getValue() != false && Colors.INSTANCE.rainbow.getValue() != false));
    public Setting<String> prefix = this.register(new Setting<String>("Prefix", ".").setRenderName(true));
    public Setting<Integer> red = this.register(new Setting<Integer>("Red", 0, 0, 255));
    public Setting<Integer> green = this.register(new Setting<Integer>("Green", 255, 0, 255));
    public Setting<Integer> blue = this.register(new Setting<Integer>("Blue", 255, 0, 255));
    public Setting<Integer> hoverAlpha = this.register(new Setting<Integer>("Alpha", 165, 0, 255));
    public Setting<Integer> alpha = this.register(new Setting<Integer>("HoverAlpha", 240, 0, 255));
    public Setting<Integer> backgroundAlpha = this.register(new Setting<Integer>("BackgroundAlpha", 145, 0, 255));
    public Setting<Boolean> customFov = this.register(new Setting<Boolean>("CustomFov", false));
    public Setting<Float> fov = this.register(new Setting<Object>("Fov", Float.valueOf(135.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f), v -> this.customFov.getValue()));
    public Setting<Boolean> openCloseChange = this.register(new Setting<Boolean>("Open/Close", true));
    public Setting<String> open = this.register(new Setting<Object>("Open:", "+", v -> this.openCloseChange.getValue()).setRenderName(true));
    public Setting<String> close = this.register(new Setting<Object>("Close:", "-", v -> this.openCloseChange.getValue()).setRenderName(true));
    public Setting<String> moduleButton = this.register(new Setting<Object>("Buttons:", "", v -> this.openCloseChange.getValue() == false).setRenderName(true));
    public Setting<Boolean> devSettings = this.register(new Setting<Boolean>("DevSettings", true));
    public Setting<Integer> topRed = this.register(new Setting<Object>("TopRed", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.devSettings.getValue()));
    public Setting<Integer> topGreen = this.register(new Setting<Object>("TopGreen", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.devSettings.getValue()));
    public Setting<Integer> topBlue = this.register(new Setting<Object>("TopBlue", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.devSettings.getValue()));
    public Setting<Integer> topAlpha = this.register(new Setting<Object>("TopAlpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.devSettings.getValue()));
    public Setting<Boolean> particles = this.register(new Setting<Boolean>("Particles", false));
    public Setting<Integer> particleLength = this.register(new Setting<Integer>("ParticleLength", 80, 0, 300, v -> this.particles.getValue()));
    public Setting<Integer> particlered = this.register(new Setting<Integer>("ParticleRed", 255, 0, 255, v -> this.particles.getValue()));
    public Setting<Integer> particlegreen = this.register(new Setting<Integer>("ParticleGreen", 255, 0, 255, v -> this.particles.getValue()));
    public Setting<Integer> particleblue = this.register(new Setting<Integer>("ParticleBlue", 255, 0, 255, v -> this.particles.getValue()));
    public Setting<Boolean> frameSettings = this.register(new Setting<Boolean>("FrameSetting", true));
    public Setting<Integer> frameRed = this.register(new Setting<Integer>("FrameRed", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.frameSettings.getValue()));
    public Setting<Integer> frameGreen = this.register(new Setting<Integer>("FrameGreen", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.frameSettings.getValue()));
    public Setting<Integer> frameBlue = this.register(new Setting<Integer>("FrameBlue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.frameSettings.getValue()));
    public Setting<Integer> frameAlpha = this.register(new Setting<Integer>("FrameAlpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.frameSettings.getValue()));

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT, true, false, false);
        this.setInstance();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (this.customFov.getValue().booleanValue()) {
            ClickGui.mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, this.fov.getValue().floatValue());
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                Atrium.commandManager.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to \u00a7a" + Atrium.commandManager.getPrefix());
            }
            Atrium.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.hoverAlpha.getPlannedValue());
        }
    }

    @Override
    public void onEnable() {
        Util.mc.displayGuiScreen(new AtriumGui());
        if (this.blurEffect.getValue()) {
            ClickGui.mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
    }

    @Override
    public void onLoad() {
        if (this.colorSync.getValue().booleanValue()) {
            Atrium.colorManager.setColor(Colors.INSTANCE.getCurrentColor().getRed(), Colors.INSTANCE.getCurrentColor().getGreen(), Colors.INSTANCE.getCurrentColor().getBlue(), this.hoverAlpha.getValue());
        } else {
            Atrium.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.hoverAlpha.getValue());
        }
        Atrium.commandManager.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGui.mc.currentScreen instanceof AtriumGui)) {
            this.disable();
            if (mc.entityRenderer.getShaderGroup() != null)
                mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

    @Override
    public void onDisable() {
        if (ClickGui.mc.currentScreen instanceof AtriumGui) {
            Util.mc.displayGuiScreen(null);
        }
    }
}

