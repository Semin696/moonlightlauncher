package moonlight.api.event.events;

import moonlight.api.event.Event;

public class EventChat extends Event {

    private final String message;

    public EventChat(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
