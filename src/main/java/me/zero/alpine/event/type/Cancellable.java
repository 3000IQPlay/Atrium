package me._3000IQPlay.zero.alpine.event.type;

import me._3000IQPlay.zero.alpine.event.type.ICancellable;

public class Cancellable
implements ICancellable {
    private boolean cancelled;

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}

