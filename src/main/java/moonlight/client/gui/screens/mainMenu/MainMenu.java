package moonlight.client.gui.screens.mainMenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import org.lwjgl.glfw.GLFW;

import moonlight.api.math.MathSystem;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.Moonlight;
import moonlight.client.account.AccountManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenu extends Screen {

    private record MenuEntry(String label, Runnable action) {}

    private static final int ACCENT = new Color(138, 99, 255).getRGB();
    private static final int ACCENT_SOFT = new Color(155, 123, 255).getRGB();
    private static final int CYAN = new Color(70, 211, 255).getRGB();
    private static final int PANEL_BG = new Color(19, 19, 31).getRGB();
    private static final int TEXT_DIM = new Color(154, 154, 176).getRGB();

    private final List<MenuEntry> entries = List.of(
            new MenuEntry("Singleplayer", () -> this.client.setScreen(new SelectWorldScreen(this))),
            new MenuEntry("Multiplayer", () -> this.client.setScreen(new MultiplayerScreen(this))),
            new MenuEntry("Options...", () -> this.client.setScreen(new OptionsScreen(this, this.client.options))),
            new MenuEntry("Quit Game", () -> this.client.scheduleStop())
    );

    private record Star(float x, float y, float size, float phase, float speed) {}

    private final List<Star> stars = new ArrayList<>();

    private final float[] hover = new float[entries.size()];

    private long lastFrame;
    private float time;

    private float titleY, titleSize;

    private boolean panelOpen;
    private float panelT;
    private float chipHover;

    private float scroll;
    private boolean inputActive;
    private final StringBuilder input = new StringBuilder();

    public MainMenu() {
        super(Text.of("Moonlight"));
    }

    @Override
    public void init() {
        Random random = new Random(1337);

        stars.clear();
        for(int i = 0; i < 140; i++)
            stars.add(new Star(random.nextFloat(), random.nextFloat(),
                    0.8f + random.nextFloat() * 1.6f, random.nextFloat(), 0.4f + random.nextFloat()));

        this.time = 0;
        this.lastFrame = Util.getMeasuringTimeNano();
        this.panelOpen = false;
        this.panelT = 0;
        this.scroll = 0;
        this.inputActive = false;
        this.input.setLength(0);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = Util.getMeasuringTimeNano();
        float dt = Math.min((now - this.lastFrame) / 1_000_000_000f, 0.05f);
        this.lastFrame = now;
        this.time += dt;

        var window = MinecraftClient.getInstance().getWindow();
        float w = window.getScaledWidth(), h = window.getScaledHeight();

        renderSky(context, w, h);
        renderTitle(context, w, h);
        renderButtons(context, mouseX, mouseY, w, h, dt);
        renderFooter(context, mouseX, mouseY, w, h, dt);
        renderPanel(context, mouseX, mouseY, w, h, dt);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSky(DrawContext context, float w, float h) {
        DrawSystem.drawGradient(context, 0, 0, w, h, 0, 0, 0, 0,
                new Color(7, 7, 12).getRGB(), new Color(15, 12, 24).getRGB());

        float pulse = 1 + 0.08f * (float) Math.sin(this.time * 0.8);

        DrawSystem.drawBloom(context, w * (0.22f + 0.04f * (float) Math.sin(this.time * 0.21)),
                h * (0.28f + 0.05f * (float) Math.cos(this.time * 0.17)),
                w * 0.55f * pulse, w * 0.55f * pulse, w * 0.27f, 1.0f, 80,
                withAlpha(ACCENT, 38));

        DrawSystem.drawBloom(context, w * (0.82f + 0.03f * (float) Math.cos(this.time * 0.15)),
                h * (0.62f + 0.06f * (float) Math.sin(this.time * 0.24)),
                w * 0.45f * pulse, w * 0.45f * pulse, w * 0.22f, 1.0f, 80,
                withAlpha(CYAN, 26));

        for(Star star : this.stars) {
            float twinkle = 0.35f + 0.65f * (float) Math.abs(Math.sin(this.time * star.speed() + star.phase() * 6.28f));
            float sx = star.x() * w, sy = star.y() * h * 0.85f;

            DrawSystem.drawRectangle(context, sx, sy, star.size(), star.size(),
                    star.size() / 2, star.size() / 2, star.size() / 2, star.size() / 2,
                    withAlpha(-1, (int) (150 * twinkle)));
        }
    }

    private void renderTitle(DrawContext context, float w, float h) {
        String title = "MOONLIGHT";
        float size = Math.min(44, h * 0.11f), tracking = 5;

        var font = Moonlight.INTER_FONT.get();

        float total = 0;
        for(char c : title.toCharArray())
            total += font.getWidth(String.valueOf(c), size) + tracking;
        total -= tracking;

        float cx = (w - total) / 2;
        float cy = h * 0.12f;

        this.titleY = cy;
        this.titleSize = size;

        float glowPulse = 0.5f + 0.5f * (float) Math.sin(this.time * 1.4);

        DrawSystem.drawBloom(context, w / 2 - w * 0.18f, cy + size / 2 - size * 0.35f,
                w * 0.36f, size * 2.6f, size, 1.0f, 46, withAlpha(ACCENT, 34 + (int) (16 * glowPulse)));

        for(int i = 0; i < title.length(); i++) {
            String letter = String.valueOf(title.charAt(i));
            float letterWidth = font.getWidth(letter, size);

            float p = clamp01((this.time - 0.25f - i * 0.09f) / 0.7f);
            float eased = (float) MathSystem.easeOut((float) p);

            if(eased <= 0) { cx += letterWidth + tracking; continue; }

            float y = cy + (1 - eased) * 34;
            int color = withAlpha(lerpColor(TEXT_DIM, -1, eased), (int) (255 * eased));

            TextSystem.drawText(context, letter, cx, y, size, color);
            cx += letterWidth + tracking;
        }

        float subtitleP = clamp01((this.time - 1.15f) / 0.8f);
        if(subtitleP > 0) {
            String subtitle = "V I S U A L S";
            float subSize = 11;
            float subWidth = font.getWidth(subtitle, subSize);
            float eased = (float) MathSystem.easeOut(subtitleP);

            TextSystem.drawText(context, subtitle, (w - subWidth) / 2, cy + size + 10 + (1 - eased) * 12,
                    subSize, withAlpha(ACCENT_SOFT, (int) (220 * eased)));
        }
    }

    private void renderButtons(DrawContext context, int mouseX, int mouseY, float w, float h, float dt) {
        float bw = 190, bh = 34, gap = 10;
        float startY = Math.max(this.titleY + this.titleSize + 46, h * 0.44f);

        for(int i = 0; i < this.entries.size(); i++) {
            float appearP = clamp01((this.time - (0.55f + i * 0.1f)) / 0.45f);
            float eased = (float) MathSystem.easeOut(appearP);

            if(eased <= 0) continue;

            boolean hovered = mouseX >= (w - bw) / 2 && mouseX <= (w + bw) / 2
                    && mouseY >= startY + i * (bh + gap) && mouseY <= startY + i * (bh + gap) + bh;

            this.hover[i] = approach(this.hover[i], hovered ? 1 : 0, dt * 9);

            float hw = bw + this.hover[i] * 10;
            float hx = (w - hw) / 2;
            float hy = startY + i * (bh + gap) + (1 - eased) * 42;
            float alpha = eased;

            if(this.hover[i] > 0.01f)
                DrawSystem.drawBloom(context, hx, hy + bh / 2 - 12, hw, 24, 12, 1.0f, 26,
                        withAlpha(ACCENT, (int) (55 * this.hover[i] * alpha)));

            DrawSystem.drawBlur(context, hx, hy, hw, bh, 13, 13, 13, 13,
                    withAlpha(PANEL_BG, (int) ((205 + this.hover[i] * 35) * alpha)));

            DrawSystem.drawBorder(context, hx, hy, hw, bh, 13, 13, 13, 13,
                    withAlpha(lerpColor(new Color(51, 51, 74).getRGB(), ACCENT, this.hover[i]),
                            (int) (255 * alpha)));

            var font = Moonlight.INTER_FONT.get();
            float textSize = 12;
            float textWidth = font.getWidth(this.entries.get(i).label(), textSize);

            TextSystem.drawText(context, this.entries.get(i).label(), (w - textWidth) / 2,
                    hy + bh / 2 - textSize / 2 + 1, textSize,
                    withAlpha(lerpColor(new Color(200, 200, 216).getRGB(), -1, this.hover[i]), (int) (255 * alpha)));
        }
    }

    private void renderFooter(DrawContext context, int mouseX, int mouseY, float w, float h, float dt) {
        float appearP = (float) MathSystem.easeOut(clamp01((this.time - 1.0f) / 0.6f));
        if(appearP <= 0) return;

        var font = Moonlight.INTER_FONT.get();
        String brand = "Moonlight Visuals 2026";
        float brandSize = 11;
        float brandWidth = font.getWidth(brand, brandSize);

        TextSystem.drawText(context, brand, 14, h - brandSize - 12 + (1 - appearP) * 14, brandSize,
                withAlpha(TEXT_DIM, (int) (200 * appearP)));

        float cw = 160, ch = 34;
        float cx = w - cw - 14, cy = h - ch - 12;

        boolean hovered = mouseX >= cx && mouseX <= cx + cw && mouseY >= cy && mouseY <= cy + ch;
        this.chipHover = approach(this.chipHover, hovered || this.panelOpen ? 1 : 0, dt * 9);

        float easedChip = (float) MathSystem.easeOut(clamp01((this.time - 0.9f) / 0.5f));
        float chy = cy + (1 - easedChip) * 40;

        DrawSystem.drawBlur(context, cx, chy, cw, ch, 12, 12, 12, 12,
                withAlpha(PANEL_BG, (int) ((205 + this.chipHover * 35) * appearP * easedChip)));

        DrawSystem.drawBorder(context, cx, chy, cw, ch, 12, 12, 12, 12,
                withAlpha(lerpColor(new Color(51, 51, 74).getRGB(), ACCENT, this.chipHover),
                        (int) (255 * easedChip)));

        String name = AccountManager.get().getCurrent();
        String initial = name.substring(0, 1).toUpperCase();

        DrawSystem.drawRectangle(context, cx + 10, chy + 7, 20, 20, 10, 10, 10, 10,
                withAlpha(ACCENT, (int) (235 * easedChip)));
        TextSystem.drawText(context, initial, cx + 10 + 10 - font.getWidth(initial, 11) / 2,
                chy + 7 + 10 - 5.5f, 11, withAlpha(-1, (int) (255 * easedChip)));

        TextSystem.drawText(context, name, cx + 38, chy + ch / 2 - 6, 11,
                withAlpha(lerpColor(new Color(200, 200, 216).getRGB(), -1, this.chipHover),
                        (int) (255 * easedChip)));

        String arrow = this.panelT > 0.5f ? "<" : ">";
        TextSystem.drawText(context, arrow, cx + cw - 16, chy + ch / 2 - 6, 11,
                withAlpha(TEXT_DIM, (int) (255 * easedChip)));
    }

    private void renderPanel(DrawContext context, int mouseX, int mouseY, float w, float h, float dt) {
        this.panelT = approach(this.panelT, this.panelOpen ? 1 : 0, dt * 7);

        if(this.panelT < 0.001f) return;

        float eased = (float) MathSystem.easeOut(this.panelT);

        float pw = 300, ph = Math.min(h - 100, 470);
        float px = w - 14 - pw + (pw + 30) * (1 - eased);
        float py = (h - ph) / 2;

        var font = Moonlight.INTER_FONT.get();

        DrawSystem.drawBlur(context, px, py, pw, ph, 18, 18, 18, 18, withAlpha(PANEL_BG, (int) (245 * eased)));
        DrawSystem.drawBorder(context, px, py, pw, ph, 18, 18, 18, 18, withAlpha(new Color(58, 58, 84).getRGB(), (int) (255 * eased)));

        TextSystem.drawText(context, "ACCOUNTS", px + 18, py + 16, 16,
                withAlpha(ACCENT_SOFT, (int) (255 * eased)));

        String closeX = "X";
        float closeSize = 13;
        boolean closeHovered = mouseX >= px + pw - 32 && mouseX <= px + pw - 16
                && mouseY >= py + 14 && mouseY <= py + 14 + closeSize + 4;

        TextSystem.drawText(context, closeX, px + pw - 16 - font.getWidth(closeX, closeSize), py + 16, closeSize,
                withAlpha(closeHovered ? new Color(255, 102, 128).getRGB() : TEXT_DIM, (int) (255 * eased)));

        float listY = py + 48, listH = ph - 48 - 96;

        List<String> accounts = AccountManager.get().getAccounts();
        float rowH = 44, gap = 8;
        float contentH = accounts.size() * (rowH + gap) - gap;
        float minScroll = Math.min(0, listH - contentH);
        this.scroll = clamp(this.scroll, minScroll, 0);

        DrawSystem.drawScissor(px, listY, pw, listH, () -> {
            for(int i = 0; i < accounts.size(); i++) {
                String accName = accounts.get(i);
                boolean selected = accName.equals(AccountManager.get().getCurrent());

                float rx = px + 14;
                float ry = listY + 6 + this.scroll + i * (rowH + gap);
                float rw = pw - 28;

                if(ry + rowH < listY || ry > listY + listH) continue;

                boolean rowHovered = mouseX >= rx && mouseX <= rx + rw && mouseY >= ry && mouseY <= ry + rowH;

                DrawSystem.drawRectangle(context, rx, ry, rw, rowH, 12, 12, 12, 12,
                        withAlpha(selected ? withAlpha(ACCENT, 40) : withAlpha(-1, 12), (int) (255 * eased)));

                DrawSystem.drawBorder(context, rx, ry, rw, rowH, 12, 12, 12, 12,
                        withAlpha(selected ? ACCENT : rowHovered ? new Color(90, 90, 130).getRGB() : new Color(44, 44, 64).getRGB(),
                                (int) (255 * eased)));

                DrawSystem.drawRectangle(context, rx + 10, ry + rowH / 2 - 8, 16, 16, 8, 8, 8, 8,
                        withAlpha(ACCENT, (int) ((selected ? 235 : 120) * eased)));

                TextSystem.drawText(context, accName, rx + 36, ry + 8, 13,
                        withAlpha(selected ? -1 : new Color(210, 210, 224).getRGB(), (int) (255 * eased)));

                TextSystem.drawText(context, selected ? "Active" : "Click to select", rx + 36, ry + 25, 9,
                        withAlpha(selected ? ACCENT_SOFT : TEXT_DIM, (int) (220 * eased)));

                if(rowHovered && !selected) {
                    TextSystem.drawText(context, "x", rx + rw - 16, ry + rowH / 2 - 6, 12,
                            withAlpha(new Color(255, 102, 128).getRGB(), (int) (255 * eased)));
                }
            }
        });

        float inputY = py + ph - 84, inputH = 34;
        float inputW = pw - 28 - 76;
        float ix = px + 14;

        boolean inputHovered = mouseX >= ix && mouseX <= ix + inputW && mouseY >= inputY && mouseY <= inputY + inputH;

        DrawSystem.drawRectangle(context, ix, inputY, inputW, inputH, 10, 10, 10, 10,
                withAlpha(withAlpha(-1, 10), (int) (255 * eased)));

        DrawSystem.drawBorder(context, ix, inputY, inputW, inputH, 10, 10, 10, 10,
                withAlpha(this.inputActive ? ACCENT : inputHovered ? new Color(90, 90, 130).getRGB() : new Color(44, 44, 64).getRGB(),
                        (int) (255 * eased)));

        String shown = this.input.isEmpty() && !this.inputActive ? "New nickname..."
                : this.input.toString();

        TextSystem.drawText(context, shown, ix + 10, inputY + inputH / 2 - 6, 12,
                withAlpha(this.input.isEmpty() && !this.inputActive ? TEXT_DIM : -1, (int) (235 * eased)));

        if(this.inputActive && (this.time * 3) % 2 < 1) {
            float caretX = ix + 10 + font.getWidth(this.input.toString(), 12) + 1;
            DrawSystem.drawRectangle(context, caretX, inputY + 8, 1.4f, inputH - 16, 1, 1, 1, 1,
                    withAlpha(ACCENT_SOFT, (int) (255 * eased)));
        }

        float addW = 62, addX = px + pw - 14 - addW;
        boolean addHovered = mouseX >= addX && mouseX <= addX + addW && mouseY >= inputY && mouseY <= inputY + inputH;

        DrawSystem.drawRectangle(context, addX, inputY, addW, inputH, 10, 10, 10, 10,
                withAlpha(lerpColor(new Color(74, 56, 148).getRGB(), ACCENT, addHovered ? 1 : 0), (int) (255 * eased)));

        String addLabel = "Add";
        TextSystem.drawText(context, addLabel, addX + addW / 2 - font.getWidth(addLabel, 12) / 2,
                inputY + inputH / 2 - 6, 12, withAlpha(-1, (int) (255 * eased)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button != 0) return super.mouseClicked(mouseX, mouseY, button);

        var window = MinecraftClient.getInstance().getWindow();
        float w = window.getScaledWidth(), h = window.getScaledHeight();

        float bw = 190, bh = 34, gap = 10;
        float startY = Math.max(this.titleY + this.titleSize + 46, h * 0.44f);

        for(int i = 0; i < this.entries.size(); i++) {
            if(mouseX >= (w - bw) / 2 && mouseX <= (w + bw) / 2
                    && mouseY >= startY + i * (bh + gap) && mouseY <= startY + i * (bh + gap) + bh) {
                this.entries.get(i).action().run();
                return true;
            }
        }

        float cw = 160, ch = 34, cx = w - cw - 14, cy = h - ch - 12;
        if(mouseX >= cx && mouseX <= cx + cw && mouseY >= cy && mouseY <= cy + ch) {
            this.panelOpen = !this.panelOpen;
            return true;
        }

        if(this.panelT > 0.01f) {
            float eased = (float) MathSystem.easeOut(this.panelT);
            float pw = 300, ph = Math.min(h - 100, 470);
            float px = w - 14 - pw + (pw + 30) * (1 - eased);
            float py = (h - ph) / 2;

            if(mouseX >= px + pw - 32 && mouseX <= px + pw - 16 && mouseY >= py + 14 && mouseY <= py + 32) {
                this.panelOpen = false;
                return true;
            }

            float listY = py + 48, listH = ph - 48 - 96;
            List<String> accounts = AccountManager.get().getAccounts();
            float rowH = 44, gapR = 8;

            for(int i = 0; i < accounts.size(); i++) {
                float rx = px + 14, ry = listY + 6 + this.scroll + i * (rowH + gapR), rw = pw - 28;

                if(mouseX >= rx && mouseX <= rx + rw && mouseY >= ry && mouseY <= ry + rowH) {
                    String accName = accounts.get(i);
                    boolean selected = accName.equals(AccountManager.get().getCurrent());

                    if(selected) continue;

                    boolean delZone = mouseX >= rx + rw - 24;
                    if(delZone) AccountManager.get().remove(accName);
                    else AccountManager.get().apply(accName);

                    return true;
                }
            }

            float inputY = py + ph - 84, inputH = 34, inputW = pw - 28 - 76, ix = px + 14;
            if(mouseX >= ix && mouseX <= ix + inputW && mouseY >= inputY && mouseY <= inputY + inputH) {
                this.inputActive = true;
                return true;
            }

            float addW = 62, addX = px + pw - 14 - addW;
            if(mouseX >= addX && mouseX <= addX + addW && mouseY >= inputY && mouseY <= inputY + inputH) {
                submitInput();
                return true;
            }

            this.inputActive = false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if(this.panelT > 0.01f) {
            this.scroll += (float) vertical * 22;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(this.inputActive && chr >= ' ' && chr < 127 && this.input.length() < 16) {
            this.input.append(chr);
            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch(keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                if(this.inputActive) this.inputActive = false;
                else if(this.panelOpen) this.panelOpen = false;
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if(this.inputActive) {
                    submitInput();
                    return true;
                }
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if(this.inputActive && !this.input.isEmpty()) {
                    this.input.setLength(this.input.length() - 1);
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void submitInput() {
        if(!this.input.isEmpty()) {
            AccountManager.get().add(this.input.toString());
            this.input.setLength(0);
        }

        this.inputActive = false;
    }

    private static float approach(float value, float target, float speed) {
        return value + (target - value) * Math.min(speed, 1);
    }

    private static float clamp01(float v) {
        return Math.max(0, Math.min(1, v));
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    private static int lerpColor(int a, int b, float t) {
        t = clamp01(t);

        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;

        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);

        return (r << 16) | (g << 8) | bl;
    }

}
