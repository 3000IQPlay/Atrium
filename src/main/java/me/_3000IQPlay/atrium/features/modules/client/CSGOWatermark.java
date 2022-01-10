package me._3000IQPlay.atrium.features.modules.client;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.event.events.Render2DEvent;
import me._3000IQPlay.atrium.features.modules.Module;
import me._3000IQPlay.atrium.features.setting.Setting;
import me._3000IQPlay.atrium.util.ColorUtil;
import me._3000IQPlay.atrium.util.RenderUtil;
import me._3000IQPlay.atrium.util.Timer;

public class CSGOWatermark extends Module {

    Timer delayTimer = new Timer();
    public Setting<Integer> X = this.register(new Setting("WatermarkX", 0, 0, 300));
    public Setting<Integer> Y = this.register(new Setting("WatermarkY", 0, 0, 300));
    public float hue;
    public int red = 1;
    public int green = 1;
    public int blue = 1;

    private String message = "";
    public CSGOWatermark() {
        super("CSGOWatermark", "CS:GO", Module.Category.CLIENT, true, false, false);
    }

    @Override
    public void onRender2D ( Render2DEvent event ) {
        drawCsgoWatermark();
    }

    public void drawCsgoWatermark () {
        int padding = 5;
        message = "Atrium v1.5.6 | " + mc.player.getName() + " | " + Atrium.serverManager.getPing() + "ms";
        Integer textWidth = mc.fontRenderer.getStringWidth(message); // thanks to wurst+ 3
        Integer textHeight = mc.fontRenderer.FONT_HEIGHT; // taken from wurst+ 3
        RenderUtil.drawRectangleCorrectly(X.getValue() - 4, Y.getValue() - 4, textWidth + 16, textHeight + 12, ColorUtil.toRGBA(0, 255, 255, 255));
        RenderUtil.drawRectangleCorrectly(X.getValue(), Y.getValue(), textWidth + 4, textHeight + 4, ColorUtil.toRGBA(30, 30, 30, 255));
        RenderUtil.drawRectangleCorrectly(X.getValue(), Y.getValue(), textWidth + 8, textHeight + 4, ColorUtil.toRGBA(30, 30, 30, 255));
        mc.fontRenderer.drawString(message, X.getValue() + 3, Y.getValue() + 3, ColorUtil.toRGBA(255, 255, 255, 255), false);
    }
}
