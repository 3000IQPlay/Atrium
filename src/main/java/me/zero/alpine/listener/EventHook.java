package me._3000IQPlay.zero.alpine.listener;

@FunctionalInterface
public interface EventHook<T> {
    public void invoke(T var1);
}

