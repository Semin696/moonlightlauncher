package moonlight.client.gui.screens.clickGui;

import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import moonlight.Moonlight;
import moonlight.api.handler.other.MouseSystem;
import moonlight.api.math.MathSystem;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.client.addon.Addon;
import moonlight.client.addon.Type;
import moonlight.client.gui.IStyle;
import moonlight.client.gui.widget.Widget;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen implements IStyle {

    public static Type currentType = Type.VISUAL;

    private static final int ACCENT = new Color(138, 99, 255).getRGB();
    private static final int CARD_BG = new Color(22, 22, 34, 215).getRGB();
    private static final int TEXT_DIM = new Color(120, 120, 145).getRGB();

    private record Tab(Type type, String label) {}

    private static final List<Tab> TABS = List.of(
            new Tab(Type.VISUAL, "Visuals"),
            new Tab(Type.HUD, "HUD"),
            new Tab(Type.UTILS, "Utilities")
    );

    private final Window window = MinecraftClient.getInstance().getWindow();

    private float x, y, width, height;

    private float time;
    private float scale;
    private float blur;

    private float contentAlpha;
    private float switchAnim; // chevron expand 0..1

    private final List<Float> hovers = new ArrayList<>();
    private final List<Float> switches = new ArrayList<>();

    private boolean searchActive;
    private final StringBuilder search = new StringBuilder();

    private float scroll;

    public ClickGui() {
        super(Text.of(StringUtil.capitalize(Moonlight.MOD_ID)));

        int count = Moonlight.addonSystem.getModules().size();
        for(int i = 0; i < count; i++) {
            this.hovers.add(0f);
            this.switches.add(0f);
        }
    }

    @Override
    public void init() {
        this.time = 0;
        this.scale = 0;

        assert this.client != null;
        this.blur = this.client.options.getMenuBackgroundBlurrinessValue();
        this.client.options.getMenuBackgroundBlurriness().setValue(0);
    }

    private List<Addon> visibleAddons() {
        String query = this.search.toString().toLowerCase();

        return Moonlight.addonSystem.getModulesByType(currentType).stream()
                .filter(addon -> query.isEmpty() || addon.getName().toLowerCase().contains(query))
                .toList();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.time += delta / 20;
        float t = MathHelper_clamp(this.time * 4);
        float progress = (float) MathSystem.easeBoth(t);
        this.scale = MathSystem.lerp(0.0f, 100.0f, progress);

        float w = this.window.getScaledWidth(), h = this.window.getScaledHeight();
        this.width = Math.min(w * 0.62f, 460);
        this.height = Math.min(h * 0.72f, 330);
        this.x = (w - this.width) / 2;
        this.y = (h - this.height) / 2;

        this.contentAlpha = MathHelper_clamp((this.time - 0.25f) / 0.4f);

        context.getMatrices().push();
        context.getMatrices().translate(this.x + this.width / 2, this.y + this.height / 2, 1);
        context.getMatrices().scale(this.scale / 100, this.scale / 100, 1);
        context.getMatrices().translate(-(this.x + this.width / 2), -(this.y + this.height / 2), 1);

        DrawSystem.drawBlur(context, this.x, this.y, this.width, this.height, 14, 14, 14, 14,
                new Color(14, 14, 22, 240).getRGB());
        DrawSystem.drawBorder(context, this.x, this.y, this.width, this.height, 14, 14, 14, 14,
                new Color(52, 52, 78).getRGB());

        renderHeader(context, mouseX, mouseY);
        renderCards(context, mouseX, mouseY, delta);

        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHeader(DrawContext context, int mouseX, int mouseY) {
        var font = Moonlight.INTER_FONT.get();
        float cx = this.x + 16, cy = this.y + 12;

        //tabs
        for(int i = 0; i < TABS.size(); i++) {
            Tab tab = TABS.get(i);
            boolean active = currentType.equals(tab.type());

            int color = active ? -1 : TEXT_DIM;
            if(!active && MouseSystem.isMouseOver(mouseX, mouseY, cx, cy - 2, font.getWidth(tab.label(), 12), 14))
                color = new Color(170, 170, 195).getRGB();

            TextSystem.drawText(context, tab.label(), cx, cy, 12, withAlpha(color, (int) (255 * this.contentAlpha)));

            if(active) {
                float tw = font.getWidth(tab.label(), 12);
                DrawSystem.drawRectangle(context, cx, cy + 15, tw, 2, 1, 1, 1, 1,
                        withAlpha(ACCENT, (int) (255 * this.contentAlpha)));
            }

            cx += font.getWidth(tab.label(), 12) + 6;

            if(i < TABS.size() - 1) {
                TextSystem.drawText(context, "/", cx, cy, 12, withAlpha(new Color(60, 60, 85).getRGB(), (int) (255 * this.contentAlpha)));
                cx += font.getWidth("/", 12) + 6;
            }
        }

        //search box
        float sw = 130, sh = 24;
        float sx = this.x + this.width - sw - 14, sy = this.y + 9;

        boolean hovered = MouseSystem.isMouseOver(mouseX, mouseY, sx, sy, sw, sh);

        DrawSystem.drawRectangle(context, sx, sy, sw, sh, 12, 12, 12, 12,
                withAlpha(this.searchActive ? withAlpha(ACCENT, 35) : withAlpha(-1, 12), (int) (255 * this.contentAlpha)));
        DrawSystem.drawBorder(context, sx, sy, sw, sh, 12, 12, 12, 12,
                withAlpha(this.searchActive ? ACCENT : hovered ? new Color(90, 90, 130).getRGB() : new Color(45, 45, 68).getRGB(),
                        (int) (255 * this.contentAlpha)));

        String query = this.search.isEmpty() && !this.searchActive ? "Search..." : this.search.toString();
        TextSystem.drawText(context, query, sx + 10, sy + sh / 2 - 6, 11,
                withAlpha(this.search.isEmpty() && !this.searchActive ? TEXT_DIM : -1, (int) (235 * this.contentAlpha)));

        if(this.searchActive && (this.time * 3) % 2 < 1) {
            float caretX = sx + 10 + font.getWidth(this.search.toString(), 11) + 1;
            DrawSystem.drawRectangle(context, caretX, sy + 5, 1.4f, sh - 10, 1, 1, 1, 1,
                    withAlpha(ACCENT, (int) (255 * this.contentAlpha)));
        }
    }

    private void renderCards(DrawContext context, int mouseX, int mouseY, float delta) {
        List<Addon> addons = visibleAddons();

        float pad = 16;
        float cardW = (this.width - pad * 2 - 14) / 2;
        float cardH = 30, gapX = 14, gapY = 10;

        float contentY = this.y + 44;
        float contentH = this.height - 44 - 30;

        var font = Moonlight.INTER_FONT.get();

        DrawSystem.drawScissor(this.x, contentY - this.scroll, this.width, contentH, () -> {
            int rendered = 0;

            for(Addon addon : addons) {
                int i = Moonlight.addonSystem.getModules().indexOf(addon);

                int col = rendered % 2, row = rendered / 2;
                float cx = this.x + pad + col * (cardW + gapX);
                float cy = contentY + this.scroll + row * (cardH + gapY + (this.switchAnim > 0.5f ? widgetOffset(addon) : 0));

                boolean hovered = MouseSystem.isMouseOver(mouseX, mouseY, cx, cy, cardW, cardH);
                this.hovers.set(i, this.hovers.get(i) + ((hovered ? 1f : 0f) - this.hovers.get(i)) * 0.2f);
                this.switches.set(i, this.switches.get(i) + ((addon.isEnable() ? 1f : 0f) - this.switches.get(i)) * 0.25f);

                float hv = this.hovers.get(i);

                DrawSystem.drawBlur(context, cx, cy, cardW, cardH, 10, 10, 10, 10,
                        withAlpha(CARD_BG, (int) ((215 + hv * 25) * this.contentAlpha)));
                DrawSystem.drawBorder(context, cx, cy, cardW, cardH, 10, 10, 10, 10,
                        withAlpha(hv > 0.02f ? lerpColor(new Color(48, 48, 72).getRGB(), ACCENT, hv * 0.6f) : new Color(48, 48, 72).getRGB(),
                                (int) (255 * this.contentAlpha)));

                TextSystem.drawText(context, addon.getName(), cx + 11, cy + cardH / 2 - 5.5f, 11,
                        withAlpha(addon.isEnable() ? -1 : TEXT_DIM, (int) (255 * this.contentAlpha)));

                //switch
                float swW = 28, swH = 15;
                float swX = cx + cardW - swW - 9, swY = cy + cardH / 2 - swH / 2;
                float swT = this.switches.get(i);

                int trackColor = lerpColor(new Color(45, 45, 65).getRGB(), ACCENT, swT);
                DrawSystem.drawRectangle(context, swX, swY, swW, swH, swH / 2, swH / 2, swH / 2, swH / 2,
                        withAlpha(trackColor, (int) (255 * this.contentAlpha)));

                float knob = MathSystem.lerp(2f, swW - 13, swT);
                DrawSystem.drawRectangle(context, swX + knob, swY + 2, swH - 4, swH - 4, (swH - 4) / 2, (swH - 4) / 2, (swH - 4) / 2, (swH - 4) / 2,
                        withAlpha(-1, (int) (255 * this.contentAlpha)));

                //widgets when expanded
                if(this.switchAnim > 0.5f) {
                    float wy = cy + cardH + 6;
                    for(Widget widget : addon.widgets) {
                        widget.render(context, cx + 11, wy, delta);
                        widget.render(context, cx + 11, wy, cardW - 22, 7, delta);
                        wy += 23;
                    }
                }

                rendered++;
            }
        });

        //chevron
        String chevron = this.switchAnim > 0.5f ? "^" : "v";
        float chW = font.getWidth(chevron, 12);
        float chX = this.x + this.width / 2 - chW / 2, chY = this.y + this.height - 22;

        boolean chHovered = MouseSystem.isMouseOver(mouseX, mouseY, chX - 8, chY - 4, chW + 16, 18);
        TextSystem.drawText(context, chevron, chX, chY, 12,
                withAlpha(chHovered ? ACCENT : TEXT_DIM, (int) (255 * this.contentAlpha)));
    }

    private float widgetOffset(Addon addon) {
        return addon.widgets.size() * 23;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button != 0) return true;

        var font = Moonlight.INTER_FONT.get();

        //search box
        float sw = 130, sh = 24;
        float sx = this.x + this.width - sw - 14, sy = this.y + 9;

        if(MouseSystem.isMouseOver(mouseX, mouseY, sx, sy, sw, sh)) {
            this.searchActive = true;
            return true;
        }

        this.searchActive = false;

        //tabs
        float cx = this.x + 16, cy = this.y + 12;
        for(int i = 0; i < TABS.size(); i++) {
            Tab tab = TABS.get(i);
            if(MouseSystem.isMouseOver(mouseX, mouseY, cx, cy - 2, font.getWidth(tab.label(), 12), 14)) {
                if(!currentType.equals(tab.type())) {
                    currentType = tab.type();
                    this.scroll = 0;
                }
                return true;
            }
            cx += font.getWidth(tab.label(), 12) + 6;
            if(i < TABS.size() - 1) cx += font.getWidth("/", 12) + 6;
        }

        //cards
        List<Addon> addons = visibleAddons();

        float pad = 16;
        float cardW = (this.width - pad * 2 - 14) / 2;
        float cardH = 30, gapX = 14, gapY = 10;
        float contentY = this.y + 44;

        int rendered = 0;

        for(Addon addon : addons) {
            int col = rendered % 2, row = rendered / 2;
            float cx2 = this.x + pad + col * (cardW + gapX);
            float cy2 = contentY + this.scroll + row * (cardH + gapY + (this.switchAnim > 0.5f ? widgetOffset(addon) : 0));

            if(MouseSystem.isMouseOver(mouseX, mouseY, cx2, cy2, cardW, cardH)) {
                Moonlight.addonSystem.logic.toggleModule(addon);
                return true;
            }

            if(this.switchAnim > 0.5f) {
                float wy = cy2 + cardH + 6;
                for(Widget widget : addon.widgets) {
                    if(MouseSystem.isMouseOver(mouseX, mouseY, cx2 + 11, wy, cardW - 22, 20))
                        widget.mouseClicked(mouseX, mouseY, button);
                    wy += 23;
                }
            }

            rendered++;
        }

        //chevron
        float chW = font.getWidth(this.switchAnim > 0.5f ? "^" : "v", 12);
        float chX = this.x + this.width / 2 - chW / 2, chY = this.y + this.height - 22;

        if(MouseSystem.isMouseOver(mouseX, mouseY, chX - 8, chY - 4, chW + 16, 18)) {
            this.switchAnim = this.switchAnim > 0.5f ? 0 : 1;
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        this.scroll += (float) vertical * 14;
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(this.searchActive && chr >= ' ' && chr < 127 && this.search.length() < 24) {
            this.search.append(chr);
            this.scroll = 0;
            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        if(this.searchActive) {
            if(keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.search.isEmpty()) {
                this.search.setLength(this.search.length() - 1);
                this.scroll = 0;
                return true;
            }
            if(keyCode == GLFW.GLFW_KEY_ENTER) {
                this.searchActive = false;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.options.getMenuBackgroundBlurriness().setValue((int) this.blur);
        super.close();
    }

    private static float MathHelper_clamp(float v) {
        return Math.max(0, Math.min(1, v));
    }

    private static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));

        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;

        return (((int) (ar + (br - ar) * t)) << 16) | (((int) (ag + (bg - ag) * t)) << 8) | (int) (ab + (bb - ab) * t);
    }

}
