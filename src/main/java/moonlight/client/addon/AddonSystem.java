package moonlight.client.addon;

import moonlight.client.addon.addons.hud.Keybind;
import moonlight.client.addon.addons.hud.Logotype;
import moonlight.client.addon.addons.hud.MusicBar;
import moonlight.client.addon.addons.hud.Potions;
import moonlight.client.addon.addons.hud.TargetInfo;
import moonlight.client.addon.addons.visual.Gamma;
import moonlight.client.addon.addons.visual.Particles;
import moonlight.client.gui.widget.IWidget;
import moonlight.client.gui.widget.Widget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddonSystem {

    public final Logic logic;

    private final List<Addon> addons = new ArrayList<>();

    public AddonSystem() {
        this.logic = new Logic();

        this.addons.add(new Logotype());
        this.addons.add(new Keybind());
        this.addons.add(new Potions());
        this.addons.add(new TargetInfo());
        this.addons.add(new Gamma());
        this.addons.add(new Particles());
        this.addons.add(new MusicBar());
    }

    public List<Addon> getModules() {
        return addons;
    }

    public Addon getModulesByName(String name) {
        return this.addons.stream().filter(addon -> addon.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public List<Addon> getModulesByType(Type type) {
        return this.addons.stream().filter(addon -> addon.getModuleType().equals(type))
                .collect(Collectors.toList());
    }

    public static class Logic {

        public List<Widget> getWidgets(Addon addon) {
            return addon.widgets;
        }

        public void toggleModule(Addon addon) {
            addon.setEnable(!addon.isEnable());

            if(addon.isEnable()) addon.enable(); else addon.disable();
        }

        public boolean isSetting(Module module) {
            Class<?> clazz = module.getClass();

            for(Field field : clazz.getDeclaredFields())
                if(field.isAnnotationPresent(IWidget.class))
                    return true;

            return false;
        }

    }

}
