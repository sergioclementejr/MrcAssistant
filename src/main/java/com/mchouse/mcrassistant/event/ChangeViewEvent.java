package com.mchouse.mcrassistant.event;

import com.mchouse.mcrassistant.enums.ViewEnum;
import javafx.event.Event;
import javafx.event.EventType;
import lombok.Getter;

public class ChangeViewEvent extends Event {
    public static final EventType<ChangeViewEvent> CHANGE_VIEW = new EventType<>(Event.ANY, "ChangeViewEvent");

    @Getter
    private final ViewEnum nextView;
    public ChangeViewEvent(ViewEnum nextView) {
        super(CHANGE_VIEW);
        this.nextView = nextView;
    }

    public ChangeViewEvent() {
        super(CHANGE_VIEW);
        this.nextView = null;
    }
}
