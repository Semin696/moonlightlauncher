package moonlight.api.event.events;

import moonlight.api.event.Event;

public class EventMessage extends Event {

    private final String author;
    private final String message;

    public EventMessage(String author, String message) {
        this.author = author;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

}
