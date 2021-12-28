package me._3000IQPlay.atrium.event.events;

import me._3000IQPlay.atrium.event.EventStage;

public class KeyEvent
        extends EventStage {
    public boolean info;
    public boolean pressed;

    public KeyEvent(int stage, boolean info, boolean pressed) {
        super(stage);
        this.info = info;
        this.pressed = pressed;
    }
}

