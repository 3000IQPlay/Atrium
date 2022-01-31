package me._3000IQPlay.atrium.features.gui;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.features.gui.components.Component;
import me._3000IQPlay.atrium.features.gui.components.items.Item;
import me._3000IQPlay.atrium.features.gui.particle.Snow;
import me._3000IQPlay.atrium.features.gui.components.items.DescriptionDisplay;
import me._3000IQPlay.atrium.features.gui.particle.ParticleSystem;
import me._3000IQPlay.atrium.features.gui.particle.Particle;
import me._3000IQPlay.atrium.features.modules.client.ClickGui;
import me._3000IQPlay.atrium.features.gui.components.items.buttons.ModuleButton;
import me._3000IQPlay.atrium.features.modules.Module;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class AtriumGui
        extends GuiScreen {
    private static AtriumGui atriumGui;
    private static AtriumGui INSTANCE;
    private static DescriptionDisplay descriptionDisplay;

    static {
        INSTANCE = new AtriumGui();
        descriptionDisplay = new DescriptionDisplay("", 0.0f, 0.0f);
    }

    private final ArrayList<Component> components = new ArrayList();
    public ParticleSystem particleSystem;
    private ArrayList<Snow> _snowList = new ArrayList<Snow>();
    private AtriumGui ClickGuiMod;

    public AtriumGui() {
        this.setInstance();
        this.load();
    }

    public static AtriumGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AtriumGui();
        }
        return INSTANCE;
    }

    public static AtriumGui getClickGui() {
        return AtriumGui.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        int x = -109;
        Random random = new Random();
        {
            for (int i = 0; i < 100; ++i) {
                for (int y = 0; y < 3; ++y) {
                    Snow snow = new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1);
                    _snowList.add(snow);
                }
            }
        }

        for (final Module.Category category : Atrium.moduleManager.getCategories()) {
            this.components.add(new Component(category.getName(), x += 115, 4, true) {

                @Override
                public void setupItems() {
                    Atrium.moduleManager.getModulesByCategory(category).forEach(module -> {
                        if (!module.hidden) {
                            this.addButton(new ModuleButton(module));
                        }
                    });
                }
            });
        }
        this.components.forEach(components -> components.getItems().sort((item1, item2) -> item1.getName().compareTo(item2.getName())));
    }

    public void updateModule(Module module) {
        block0:
        for (Component component : this.components) {
            for (Item item : component.getItems()) {
                if (!(item instanceof ModuleButton)) continue;
                ModuleButton button = (ModuleButton) item;
                Module mod = button.getModule();
                if (module == null || !module.equals(mod)) continue;
                button.initSettings();
                continue block0;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ClickGui clickGui = ClickGui.getInstance();
        descriptionDisplay.setDraw(false);
        this.checkMouseWheel();
        this.drawDefaultBackground();
        this.components.forEach(components -> components.drawScreen(mouseX, mouseY, partialTicks));
        if (descriptionDisplay.shouldDraw() && clickGui.moduleDescription.getValue().booleanValue()) {
            descriptionDisplay.drawScreen(mouseX, mouseY, partialTicks);
        }
        final ScaledResolution res = new ScaledResolution(mc);
        if (!_snowList.isEmpty() && ClickGui.getInstance().snowing.getValue()) {
            _snowList.forEach(snow -> snow.Update(res));
        }
        if (this.particleSystem != null && ClickGui.getInstance().particles.getValue()) {
            this.particleSystem.render(mouseX, mouseY);
        } else {
            this.particleSystem = new ParticleSystem(new ScaledResolution(this.mc));
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        this.components.forEach(components -> components.mouseClicked(mouseX, mouseY, clickedButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        this.components.forEach(components -> components.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public final ArrayList<Component> getComponents() {
        return this.components;
    }

    public void checkMouseWheel() {
        final int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            if (ClickGui.getInstance().scroll.getValue()) {
                this.components.forEach(component -> component.setY(component.getY() - ClickGui.getInstance().scrollval.getValue()));
            }
        } else if (dWheel > 0 && ClickGui.getInstance().scroll.getValue()) {
            this.components.forEach(component -> component.setY(component.getY() + ClickGui.getInstance().scrollval.getValue()));
        }
    }

    public int getTextOffset() {
        return -6;
    }

    public DescriptionDisplay getDescriptionDisplay() {
        return descriptionDisplay;
    }

    public Component getComponentByName(String name) {
        for (Component component : this.components) {
            if (!component.getName().equalsIgnoreCase(name)) continue;
            return component;
        }
        return null;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.components.forEach(component -> component.onKeyTyped(typedChar, keyCode));
    }

    public void updateScreen() {
        if (this.particleSystem != null) {
            this.particleSystem.update();
        }
    }
}


