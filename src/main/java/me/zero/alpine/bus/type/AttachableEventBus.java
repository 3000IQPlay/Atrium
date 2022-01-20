package me._3000IQPlay.zero.alpine.bus.type;

import me._3000IQPlay.zero.alpine.bus.EventBus;

public interface AttachableEventBus
extends EventBus {
    public void attach(EventBus var1);

    public void detach(EventBus var1);
}

