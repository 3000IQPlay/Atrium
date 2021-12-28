package me._3000IQPlay.atrium.features.gui.custom.loader.hwid.util;

import me._3000IQPlay.atrium.features.gui.custom.loader.hwid.HWIDAuth;

public class NoStackTraceThrowable extends RuntimeException {

    public NoStackTraceThrowable(final String msg) {
        super(msg);
        this.setStackTrace(new StackTraceElement[0]);
    }

    @Override
    public String toString() {
        return "" + HWIDAuth.getVersion();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
