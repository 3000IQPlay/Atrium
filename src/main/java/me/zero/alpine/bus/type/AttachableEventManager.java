package me._3000IQPlay.zero.alpine.bus.type;

import java.util.ArrayList;
import java.util.List;
import me._3000IQPlay.zero.alpine.bus.EventBus;
import me._3000IQPlay.zero.alpine.bus.EventManager;
import me._3000IQPlay.zero.alpine.bus.type.AttachableEventBus;
import me._3000IQPlay.zero.alpine.listener.Listenable;
import me._3000IQPlay.zero.alpine.listener.Listener;

public class AttachableEventManager
extends EventManager
implements AttachableEventBus {
    private final List<EventBus> attached = new ArrayList<EventBus>();

    @Override
    public void subscribe(Listenable listenable) {
        super.subscribe(listenable);
        if (!this.attached.isEmpty()) {
            this.attached.forEach(bus -> bus.subscribe(listenable));
        }
    }

    @Override
    public void subscribe(Listener listener) {
        super.subscribe(listener);
        if (!this.attached.isEmpty()) {
            this.attached.forEach(bus -> bus.subscribe(listener));
        }
    }

    @Override
    public void unsubscribe(Listenable listenable) {
        super.unsubscribe(listenable);
        if (!this.attached.isEmpty()) {
            this.attached.forEach(bus -> bus.unsubscribe(listenable));
        }
    }

    @Override
    public void unsubscribe(Listener listener) {
        super.unsubscribe(listener);
        if (!this.attached.isEmpty()) {
            this.attached.forEach(bus -> bus.unsubscribe(listener));
        }
    }

    @Override
    public void post(Object event) {
        super.post(event);
        if (!this.attached.isEmpty()) {
            this.attached.forEach(bus -> bus.post(event));
        }
    }

    @Override
    public void attach(EventBus bus) {
        if (!this.attached.contains(bus)) {
            this.attached.add(bus);
        }
    }

    @Override
    public void detach(EventBus bus) {
        this.attached.remove(bus);
    }
}

