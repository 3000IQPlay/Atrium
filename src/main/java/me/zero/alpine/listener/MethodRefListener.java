package me._3000IQPlay.zero.alpine.listener;

import java.util.function.Predicate;
import me._3000IQPlay.zero.alpine.listener.EventHook;
import me._3000IQPlay.zero.alpine.listener.Listener;

public class MethodRefListener<T>
extends Listener<T> {
    private Class<T> target;

    @SafeVarargs
    public MethodRefListener(Class<T> target, EventHook<T> hook, Predicate<T> ... filters) {
        super(hook, filters);
        this.target = target;
    }

    @SafeVarargs
    public MethodRefListener(Class<T> target, EventHook<T> hook, int priority, Predicate<T> ... filters) {
        super(hook, priority, filters);
        this.target = target;
    }

    @Override
    public Class<T> getTarget() {
        return this.target;
    }
}

