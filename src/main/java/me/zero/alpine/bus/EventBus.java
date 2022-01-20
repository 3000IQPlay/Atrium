package me._3000IQPlay.zero.alpine.bus;

import java.util.Arrays;
import me._3000IQPlay.zero.alpine.listener.Listenable;
import me._3000IQPlay.zero.alpine.listener.Listener;

public interface EventBus {
    public void subscribe(Listenable var1);

    public void subscribe(Listener var1);

    default public void subscribeAll(Listenable ... listenables) {
        Arrays.stream(listenables).forEach(this::subscribe);
    }

    default public void subscribeAll(Iterable<Listenable> listenables) {
        listenables.forEach(this::subscribe);
    }

    default public void subscribeAll(Listener ... listeners) {
        Arrays.stream(listeners).forEach(this::subscribe);
    }

    public void unsubscribe(Listenable var1);

    public void unsubscribe(Listener var1);

    default public void unsubscribeAll(Listenable ... listenables) {
        Arrays.stream(listenables).forEach(this::unsubscribe);
    }

    default public void unsubscribeAll(Iterable<Listenable> listenables) {
        listenables.forEach(this::unsubscribe);
    }

    default public void unsubscribeAll(Listener ... listeners) {
        Arrays.stream(listeners).forEach(this::unsubscribe);
    }

    public void post(Object var1);
}

