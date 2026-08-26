package moonlight.api.event.events;

import moonlight.api.event.Event;

public class EventTickPlayer extends Event {

    protected int tick;

    public EventTickPlayer(int tick) {
        this.tick = tick;
    }

    public int getTick() {
        return tick;
    }

}
