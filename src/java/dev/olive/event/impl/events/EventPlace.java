package dev.olive.event.impl.events;

import dev.olive.event.impl.CancellableEvent;

public class EventPlace
        extends CancellableEvent {

    private boolean shouldRightClick;
    private int slot;

    public EventPlace(final int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    public void setSlot(final int slot) {
        this.slot = slot;
    }

    public boolean isShouldRightClick() {
        return this.shouldRightClick;
    }

    public void setShouldRightClick(final boolean shouldRightClick) {
        this.shouldRightClick = shouldRightClick;
    }
}

