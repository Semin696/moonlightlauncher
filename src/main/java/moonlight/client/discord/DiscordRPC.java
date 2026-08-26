package moonlight.client.discord;

import net.fabricmc.loader.api.FabricLoader;

import moonlight.Moonlight;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class DiscordRPC {

    private static final String DEFAULT_CLIENT_ID = "1542090684264218704";

    private static final DiscordRPC INSTANCE = new DiscordRPC();

    private RandomAccessFile pipe;
    private long startTimestamp = System.currentTimeMillis() / 1000L;

    private String clientId = "";
    private String lastState = "";

    public static DiscordRPC get() {
        return INSTANCE;
    }

    public void start() {
        this.clientId = readClientId();

        Path enabledFile = FabricLoader.getInstance().getGameDir().resolve("moonlight").resolve("discord-enabled.txt");

        if(!Files.exists(enabledFile)) {
            Moonlight.LOGGER.info("Discord RPC: отключен в настройках лаунчера");
            return;
        }

        if(this.clientId.isEmpty()) {
            Moonlight.LOGGER.info("Discord RPC: client id не указан");
            return;
        }

        Thread thread = new Thread(this::run, "Moonlight-DiscordRPC");
        thread.setDaemon(true);
        thread.start();

        Moonlight.LOGGER.info("Discord RPC: запущен");
    }

    private String readClientId() {
        try {
            Path file = FabricLoader.getInstance().getGameDir().resolve("moonlight").resolve("discord-clientid.txt");

            if(Files.exists(file)) {
                String fromFile = Files.readString(file, StandardCharsets.UTF_8).trim();

                if(!fromFile.isEmpty())
                    return fromFile;
            }
        } catch (IOException ignored) {
        }

        return DEFAULT_CLIENT_ID;
    }

    private void run() {
        long lastUpdate = 0;

        while(true) {
            try {
                if(this.pipe == null)
                    connect();

                if(System.currentTimeMillis() - lastUpdate >= 15000) {
                    update();
                    lastUpdate = System.currentTimeMillis();
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                this.pipe = null;
                this.lastState = "";

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }
    }

    private void connect() throws IOException {
        for(int i = 0; i < 10; i++) {
            try {
                this.pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                break;
            } catch (IOException ignored) {
            }
        }

        if(this.pipe == null)
            throw new IOException("Discord pipe not found");

        String payload = "{\"v\":1,\"client_id\":\"" + this.clientId + "\"}";
        writeFrame(0, payload);
        readFrame();

        Moonlight.LOGGER.info("Discord RPC: подключен");
        this.startTimestamp = System.currentTimeMillis() / 1000L;
    }

    private synchronized void update() throws IOException {
        String state = currentState();

        String activity = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid()
                + ",\"activity\":{\"details\":\"Moonlight Visuals 2026\",\"state\":\"" + state
                + "\",\"timestamps\":{\"start\":" + this.startTimestamp + "},\"instance\":true}},\"nonce\":\""
                + UUID.randomUUID() + "\"}";

        writeFrame(1, activity);
        this.lastState = state;
    }

    private String currentState() {
        var client = net.minecraft.client.MinecraftClient.getInstance();

        if(client.world == null)
            return "В главном меню";

        if(client.getServer() != null)
            return "Играет в одиночном мире";

        var server = client.getCurrentServerEntry();
        return server != null ? "На сервере: " + server.address : "Играет";
    }

    private synchronized void writeFrame(int op, String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);

        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(op);
        header.putInt(bytes.length);

        this.pipe.write(header.array());
        this.pipe.write(bytes);
    }

    @SuppressWarnings("unused")
    private synchronized String readFrame() throws IOException {
        byte[] header = new byte[8];
        this.pipe.readFully(header);

        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int length = buffer.getInt(4);

        byte[] payload = new byte[length];
        this.pipe.readFully(payload);

        return new String(payload, StandardCharsets.UTF_8);
    }

}
