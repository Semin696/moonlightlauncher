package moonlight;

import com.google.common.base.Suppliers;

import net.fabricmc.loader.impl.util.StringUtil;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import moonlight.api.event.EventSystem;
import moonlight.api.java.MethodSystem;
import moonlight.api.render.msdf.MsdfFont;
import moonlight.client.addon.AddonSystem;
import moonlight.client.discord.DiscordRPC;
import moonlight.client.handler.CommandHandler;
import moonlight.client.handler.DragHandler;
import moonlight.client.handler.Keyboard;
import moonlight.launch.startup.ClientInitializer;
import moonlight.launch.startup.session.Session;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class Moonlight implements ClientInitializer {

    public static final String MOD_ID = "moonlight", MOD_LOG = StringUtil.capitalize("\u001B[35m" + MOD_ID + "\u001B[36m");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_LOG);

    public static final Supplier<MsdfFont> INTER_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("inter").data("inter").build());
    public static final Session session = new Session("2026", "01");

    public static final AddonSystem addonSystem = new AddonSystem();

    @Override
    public void onInitializeClient() {
        try {
            long executionClient = MethodSystem.executionTime(() -> {
                LOGGER.info("Build: {}", session);

                EventSystem.register(new Keyboard());
                EventSystem.register(new DragHandler());
                EventSystem.register(new CommandHandler());
                DiscordRPC.get().start();
                applyWindowBranding(MinecraftClient.getInstance());

                LOGGER.info("Init: {}", "events were successfully initialized!");
            });

            LOGGER.info("Init: {}", "initialization completed (" + executionClient + "ms)");
        } catch (Exception e) {
            LOGGER.error("Initialization failed", e);
        }
    }

    private void applyWindowBranding(MinecraftClient client) {
        client.getWindow().setTitle("Moonlight Visual");

        try (InputStream inputStream = client.getResourceManager().open(Identifier.of("moonlight", "logo.png"))) {
            NativeImage image = NativeImage.read(inputStream);

            int width = image.getWidth(), height = image.getHeight();
            ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);

            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    int argb = image.getColorArgb(x, y);

                    pixels.put((byte) ((argb >> 16) & 0xFF));
                    pixels.put((byte) ((argb >> 8) & 0xFF));
                    pixels.put((byte) (argb & 0xFF));
                    pixels.put((byte) ((argb >> 24) & 0xFF));
                }
            }

            pixels.flip();

            try (GLFWImage glfwImage = GLFWImage.malloc(); GLFWImage.Buffer buffer = GLFWImage.malloc(1)) {
                glfwImage.set(width, height, pixels);
                buffer.put(0, glfwImage);

                GLFW.glfwSetWindowIcon(client.getWindow().getHandle(), buffer);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to apply window icon", e);
        }
    }

}

