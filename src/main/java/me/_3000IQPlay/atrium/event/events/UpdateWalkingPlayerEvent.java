package me._3000IQPlay.atrium.event.events;

import me._3000IQPlay.atrium.event.EventStage;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class UpdateWalkingPlayerEvent
        extends EventStage {
    public UpdateWalkingPlayerEvent(int stage) {
        super(stage);
    }
}

