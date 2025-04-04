package dev.olive.event.impl.events;

import dev.olive.event.impl.Event;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;

@Getter
public class EventScreen
        implements Event {
    private final GuiScreen guiScreen;

    public EventScreen(GuiScreen guiScreen) {
        this.guiScreen = guiScreen;
    }

}
