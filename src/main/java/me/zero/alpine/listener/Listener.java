package me._3000IQPlay.zero.alpine.listener;

import java.util.function.Predicate;
import me._3000IQPlay.zero.alpine.listener.EventHook;
import net.jodah.typetools.TypeResolver;

public class Listener<T>
implements EventHook<T> {
    private final Class<T> target;
    private final EventHook<T> hook;
    private final Predicate<T>[] filters;
    private final int priority;

    @SafeVarargs
    public Listener(EventHook<T> hook, Predicate<T> ... filters) {
        this(hook, 0, filters);
    }

    @SafeVarargs
    public Listener(EventHook<T> hook, int priority, Predicate<T> ... filters) {
        this.hook = hook;
        this.priority = priority;
        this.target = TypeResolver.resolveRawArgument(EventHook.class, hook.getClass());
        this.filters = filters;
    }

    public Class<T> getTarget() {
        return this.target;
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    public void invoke(T event) {
        if (this.filters.length > 0) {
            for (Predicate<T> filter : this.filters) {
                if (filter.test(event)) continue;
                return;
            }
        }
        this.hook.invoke(event);
    }
}

