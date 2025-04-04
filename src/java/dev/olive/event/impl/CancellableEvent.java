package dev.olive.event.impl;

import dev.olive.event.annotations.EventTarget;

/**
 * An abstract class for cancellable events.
 * This class implements the {@link Event} and {@link Cancellable} interfaces.
 */
public abstract class CancellableEvent implements Event, Cancellable {
    private boolean cancelled;

    protected CancellableEvent() {
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    public void cancel() {
        this.cancelled = true;
    }
    @Override
    public void setCancelled(final boolean state) {
        this.cancelled = state;
    }

    @EventTarget
    public void setCancelled() {
        this.cancelled = true;
    }
}
