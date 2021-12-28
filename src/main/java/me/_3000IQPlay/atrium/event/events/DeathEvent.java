package me._3000IQPlay.atrium.event.events;

import me._3000IQPlay.atrium.event.EventStage;
import net.minecraft.entity.player.EntityPlayer;

public class DeathEvent
        extends EventStage {
    public EntityPlayer player;

    public DeathEvent(EntityPlayer player) {
        this.player = player;
    }
}

