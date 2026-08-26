package moonlight.api.event.events;

import moonlight.api.event.Event;

public class EventKeyboard extends Event {

    private final int key;
    private final int action;

    public EventKeyboard(int key, int action) {
        this.key = key;
        this.action = action;
    }

    public int getKey() {
        return key;
    }

    public int getAction() {
        return action;
    }

}
