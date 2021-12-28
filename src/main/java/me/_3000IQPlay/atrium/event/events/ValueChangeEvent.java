package me._3000IQPlay.atrium.event.events;

import me._3000IQPlay.atrium.event.EventStage;
import me._3000IQPlay.atrium.features.setting.Setting;

public class ValueChangeEvent
        extends EventStage {
    public Setting setting;
    public Object value;

    public ValueChangeEvent(Setting setting, Object value) {
        this.setting = setting;
        this.value = value;
    }
}

