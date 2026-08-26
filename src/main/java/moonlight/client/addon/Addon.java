package moonlight.client.addon;

import moonlight.api.event.EventSystem;
import moonlight.client.gui.widget.Widget;

import java.util.ArrayList;
import java.util.List;

public class Addon extends AddonSystem.Logic {

    public final List<Widget> widgets = new ArrayList<>();

    private final String name;
    private int key;

    private final Type type;

    private boolean isEnable;

    public float offset;

    public Addon(String name, int key, Type type) {
        this.name = name;
        this.key = key;
        this.type = type;
        this.isEnable = false;
    }

    public Addon(String name, Type addonType) {
        this(name, -1, addonType);
    }

    public Addon(String name, int key, Type addonType, boolean isEnable) {
        this(name, key, addonType);

        this.isEnable = isEnable;
    }

    //get
    public String getName() {
        return name;
    }

    public int getKey() {
        return key;
    }

    public Type getModuleType() {
        return type;
    }

    public boolean isEnable() {
        return isEnable;
    }

    //set
    public void setKey(int key) {
        this.key = key;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public void addWidget(Widget... widgets) {
        this.widgets.addAll(List.of(widgets));
    }

    public void enable() {
        EventSystem.register(this);
    }

    public void disable() {
        EventSystem.unregister(this);
    }
}
