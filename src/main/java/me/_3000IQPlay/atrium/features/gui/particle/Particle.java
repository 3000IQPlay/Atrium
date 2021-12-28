package me._3000IQPlay.atrium.features.gui.particle;

import java.util.concurrent.*;
import net.minecraft.client.gui.*;
import javax.vecmath.*;
import org.lwjgl.input.*;
import me._3000IQPlay.atrium.features.modules.client.ClickGui;
import me._3000IQPlay.atrium.util.RenderUtil;
import me._3000IQPlay.atrium.util.ColorUtil;

public class Particle
{
    private Vector2f pos;
    private Vector2f velocity;
    private Vector2f acceleration;
    private int alpha;
    private final int maxAlpha;
    private float size;
    
    public Particle(final Vector2f pos) {
        this.pos = pos;
        final int lowVel = -1;
        final int highVel = 1;
        final float resultXVel = lowVel + ThreadLocalRandom.current().nextFloat() * (highVel - lowVel);
        final float resultYVel = lowVel + ThreadLocalRandom.current().nextFloat() * (highVel - lowVel);
        this.velocity = new Vector2f(resultXVel, resultYVel);
        this.acceleration = new Vector2f(0.0f, 0.35f);
        this.alpha = 0;
        this.maxAlpha = ThreadLocalRandom.current().nextInt(32, 192);
        this.size = 0.5f + ThreadLocalRandom.current().nextFloat() * 1.5f;
    }
    
    public void respawn(final ScaledResolution scaledResolution) {
        this.pos = new Vector2f((float)(Math.random() * scaledResolution.getScaledWidth()), (float)(Math.random() * scaledResolution.getScaledHeight()));
    }
    
    public void update() {
        if (this.alpha < this.maxAlpha) {
            this.alpha += 8;
        }
        if (this.acceleration.getX() > 0.35f) {
            this.acceleration.setX(this.acceleration.getX() * 0.975f);
        }
        else if (this.acceleration.getX() < -0.35f) {
            this.acceleration.setX(this.acceleration.getX() * 0.975f);
        }
        if (this.acceleration.getY() > 0.35f) {
            this.acceleration.setY(this.acceleration.getY() * 0.975f);
        }
        else if (this.acceleration.getY() < -0.35f) {
            this.acceleration.setY(this.acceleration.getY() * 0.975f);
        }
        this.pos.add((Tuple2f)this.acceleration);
        this.pos.add((Tuple2f)this.velocity);
    }
    
    public void render(final int mouseX, final int mouseY) {
        if (Mouse.isButtonDown(0)) {
            final float deltaXToMouse = mouseX - this.pos.getX();
            final float deltaYToMouse = mouseY - this.pos.getY();
            if (Math.abs(deltaXToMouse) < 50.0f && Math.abs(deltaYToMouse) < 50.0f) {
                this.acceleration.setX(this.acceleration.getX() + deltaXToMouse * 0.0015f);
                this.acceleration.setY(this.acceleration.getY() + deltaYToMouse * 0.0015f);
            }
        }
        RenderUtil.drawRect(this.pos.x, this.pos.y, this.pos.x + this.size, this.pos.y + this.size, changeAlpha(ColorUtil.toRGBA(ClickGui.getInstance().particlered.getValue(), ClickGui.getInstance().particlegreen.getValue(), ClickGui.getInstance().particleblue.getValue()), this.alpha));
    }
    
    public Vector2f getPos() {
        return this.pos;
    }
    
    public void setPos(final Vector2f pos) {
        this.pos = pos;
    }
    
    public Vector2f getVelocity() {
        return this.velocity;
    }
    
    public void setVelocity(final Vector2f velocity) {
        this.velocity = velocity;
    }
    
    public static int changeAlpha(int origColor, final int userInputedAlpha) {
        origColor &= 0xFFFFFF;
        return userInputedAlpha << 24 | origColor;
    }
    
    public Vector2f getAcceleration() {
        return this.acceleration;
    }
    
    public void setAcceleration(final Vector2f acceleration) {
        this.acceleration = acceleration;
    }
    
    public int getAlpha() {
        return this.alpha;
    }
    
    public void setAlpha(final int alpha) {
        this.alpha = alpha;
    }
    
    public float getSize() {
        return this.size;
    }
    
    public void setSize(final float size) {
        this.size = size;
    }
}
