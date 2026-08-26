package moonlight.client.addon.addons.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvents;

import org.lwjgl.glfw.GLFW;

import moonlight.Moonlight;
import moonlight.api.event.EventSubscribe;
import moonlight.api.event.events.EventMouse;
import moonlight.api.event.events.EventRender2D;
import moonlight.api.handler.other.MouseSystem;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.client.addon.Addon;
import moonlight.client.addon.Type;
import moonlight.client.gui.IStyle;
import moonlight.client.gui.screens.draggableGui.comp.Draggable;

import java.awt.Color;
import java.util.List;

public class MusicBar extends Addon implements IStyle {

    private record Track(String title, String author, SoundEventsHolder holder, int duration) {}

    private interface SoundEventsHolder {
        net.minecraft.sound.SoundEvent get();
    }

    private static final List<Track> TRACKS = List.of(
            new Track("Cat", "C418", () -> SoundEvents.MUSIC_DISC_CAT.value(), 185),
            new Track("Blocks", "C418", () -> SoundEvents.MUSIC_DISC_BLOCKS.value(), 345),
            new Track("Chirp", "C418", () -> SoundEvents.MUSIC_DISC_CHIRP.value(), 185),
            new Track("Far", "C418", () -> SoundEvents.MUSIC_DISC_FAR.value(), 174),
            new Track("Mall", "C418", () -> SoundEvents.MUSIC_DISC_MALL.value(), 197),
            new Track("Mellohi", "C418", () -> SoundEvents.MUSIC_DISC_MELLOHI.value(), 96),
            new Track("Stal", "C418", () -> SoundEvents.MUSIC_DISC_STAL.value(), 155),
            new Track("Strad", "C418", () -> SoundEvents.MUSIC_DISC_STRAD.value(), 188),
            new Track("Wait", "C418", () -> SoundEvents.MUSIC_DISC_WAIT.value(), 238),
            new Track("Ward", "C418", () -> SoundEvents.MUSIC_DISC_WARD.value(), 251),
            new Track("Pigstep", "Lena Raine", () -> SoundEvents.MUSIC_DISC_PIGSTEP.value(), 148),
            new Track("Otherside", "Lena Raine", () -> SoundEvents.MUSIC_DISC_OTHERSIDE.value(), 178)
    );

    public float x = 10, y = 60;
    public float width = 210, height = 62;

    private int index;
    private boolean playing;

    private long startedAt;
    private long elapsedBefore;

    private SoundInstance current;

    private float hoverPrev, hoverPlay, hoverNext;

    public MusicBar() {
        super("Music Bar", GLFW.GLFW_KEY_Q, Type.UTILS);
        Draggable.draggables.add(new Draggable(this.getName(), this.x, this.y, this.width, this.height));
    }

    private Draggable drag() {
        for(Draggable d : Draggable.draggables)
            if(d.name.equals(this.getName())) return d;

        return Draggable.draggables.getFirst();
    }

    private float elapsed() {
        if(!this.playing) return this.elapsedBefore / 1000f;
        return (this.elapsedBefore + (System.currentTimeMillis() - this.startedAt)) / 1000f;
    }

    private void stopSound() {
        if(this.current != null) {
            MinecraftClient.getInstance().getSoundManager().stop(this.current);
            this.current = null;
        }
    }

    private void play() {
        stopSound();

        this.current = PositionedSoundInstance.master(TRACKS.get(this.index).holder().get(), 1.0f);
        MinecraftClient.getInstance().getSoundManager().play(this.current);

        this.startedAt = System.currentTimeMillis();
        this.playing = true;
    }

    private void pause() {
        this.elapsedBefore = (long) (this.elapsed() * 1000f);
        stopSound();
        this.playing = false;
    }

    private void switchTrack(int delta) {
        this.index = Math.floorMod(this.index + delta, TRACKS.size());
        this.elapsedBefore = 0;
        this.startedAt = System.currentTimeMillis();

        if(this.playing) play();
    }

    @EventSubscribe
    public void eventRender2D(EventRender2D event) {
        Draggable d = drag();

        float x = d.x, y = d.y;
        var graphics = event.getGraphics();
        var font = Moonlight.INTER_FONT.get();

        Track track = TRACKS.get(this.index);

        DrawSystem.drawBlur(graphics, x, y, this.width, this.height, 10, 10, 10, 10,
                new Color(16, 16, 26, 225).getRGB());
        DrawSystem.drawBorder(graphics, x, y, this.width, this.height, 10, 10, 10, 10,
                new Color(48, 48, 72).getRGB());

        //cover
        DrawSystem.drawGradient(graphics, x + 10, y + 10, 42, 42, 8, 8, 8, 8,
                new Color(120, 90, 220).getRGB(), new Color(70, 60, 160).getRGB());
        String coverLetter = track.title().substring(0, 1);
        TextSystem.drawText(graphics, coverLetter, x + 10 + 21 - font.getWidth(coverLetter, 16) / 2,
                y + 10 + 21 - 8, 16, -1);

        //title + author
        TextSystem.drawText(graphics, track.title(), x + 60, y + 11, 11, -1);
        TextSystem.drawText(graphics, track.author(), x + 60, y + 24, 9, new Color(150, 150, 170).getRGB());

        //progress
        float barX = x + 60, barY = y + 38, barW = this.width - 76, barH = 3.5f;
        float progress = Math.min(elapsed() / track.duration(), 1f);

        DrawSystem.drawRectangle(graphics, barX, barY, barW, barH, 2, 2, 2, 2, new Color(45, 45, 65).getRGB());
        DrawSystem.drawRectangle(graphics, barX, barY, barW * progress, barH, 2, 2, 2, 2, new Color(138, 99, 255).getRGB());

        String current = formatTime(elapsed());
        String total = "-" + formatTime(Math.max(0, track.duration() - elapsed()));

        TextSystem.drawText(graphics, current, barX, barY + 6, 8, new Color(150, 150, 170).getRGB());
        TextSystem.drawText(graphics, total, barX + barW - font.getWidth(total, 8), barY + 6, 8, new Color(150, 150, 170).getRGB());

        //controls
        float cy = y + 44, cx = x + 60;
        double mx = MinecraftClient.getInstance().mouse.getX();
        double my = MinecraftClient.getInstance().mouse.getY();

        this.hoverPrev = MouseSystem.isMouseOver(mx, my, cx - 4, cy - 6, 30, 22) ? 1 : 0;
        this.hoverPlay = MouseSystem.isMouseOver(mx, my, cx + 26, cy - 6, 30, 22) ? 1 : 0;
        this.hoverNext = MouseSystem.isMouseOver(mx, my, cx + 56, cy - 6, 30, 22) ? 1 : 0;

        int dim = new Color(160, 160, 180).getRGB();
        int bright = -1;

        TextSystem.drawText(graphics, "<<", cx, cy, 10, lerpColor(dim, bright, this.hoverPrev));
        TextSystem.drawText(graphics, this.playing ? "||" : ">", cx + 30 - font.getWidth(this.playing ? "||" : ">", 12) / 2 + 4, cy - 2, 12, lerpColor(dim, bright, this.hoverPlay));
        TextSystem.drawText(graphics, ">>", cx + 56, cy, 10, lerpColor(dim, bright, this.hoverNext));

        if(elapsed() >= track.duration())
            switchTrack(1);
    }

    @EventSubscribe
    public void eventMouse(EventMouse event) {
        if(!this.isEnable() || event.getButton() != 0 || event.getAction() != GLFW.GLFW_PRESS) return;
        if(MinecraftClient.getInstance().currentScreen != null) return;

        Draggable d = drag();
        float x = d.x, y = d.y;
        double mx = event.getMouseX(), my = event.getMouseY();

        float cy = y + 44, cx = x + 60;

        if(MouseSystem.isMouseOver(mx, my, cx - 4, cy - 6, 30, 22)) {
            switchTrack(-1);
        } else if(MouseSystem.isMouseOver(mx, my, cx + 26, cy - 6, 30, 22)) {
            if(this.playing) pause(); else play();
        } else if(MouseSystem.isMouseOver(mx, my, cx + 56, cy - 6, 30, 22)) {
            switchTrack(1);
        }
    }

    @Override
    public void disable() {
        pause();
        super.disable();
    }

    private static String formatTime(float seconds) {
        int s = (int) seconds;
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));

        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;

        return (((int) (ar + (br - ar) * t)) << 16) | (((int) (ag + (bg - ag) * t)) << 8) | (int) (ab + (bb - ab) * t);
    }

}
