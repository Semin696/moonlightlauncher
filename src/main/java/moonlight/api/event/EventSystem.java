package moonlight.api.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventSystem {

    private static final Map<Class<? extends Event>, List<Object>> listeners = new HashMap<>();

    public static void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventSubscribe.class)) {
                Class<?> eventClass = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(eventClass)) {
                    listeners.computeIfAbsent((Class<? extends Event>) eventClass, k -> new ArrayList<>()).add(listener);
                }
            }
        }
    }

    public static void unregister(Object listener) {
        listeners.values().forEach(list -> list.remove(listener));
    }

    public static <T extends Event> T post(T event) {
        List<Object> eventListeners = listeners.get(event.getClass());

        if (eventListeners != null) {
            for (Object listener : eventListeners) {
                for (Method method : listener.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(EventSubscribe.class) && method.getParameterTypes()[0].equals(event.getClass())) {
                        try {
                            method.invoke(listener, event);
                            if (event.isCanceled()) break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return event;
    }

}
