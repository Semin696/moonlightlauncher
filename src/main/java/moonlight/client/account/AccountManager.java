package moonlight.client.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import moonlight.Moonlight;
import moonlight.client.mixins.minecraft.SessionAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountManager {

    private static final AccountManager INSTANCE = new AccountManager();

    private final List<String> accounts = new ArrayList<>();
    private String current;

    public static AccountManager get() {
        return INSTANCE;
    }

    private Path file() {
        return FabricLoader.getInstance().getGameDir().resolve("moonlight").resolve("accounts.json");
    }

    public void load() {
        accounts.clear();
        current = null;

        Path nickFile = FabricLoader.getInstance().getGameDir().resolve("moonlight").resolve("launcher-nick.txt");

        if(Files.exists(nickFile)) {
            try {
                String nick = Files.readString(nickFile, StandardCharsets.UTF_8).trim();

                if(!nick.isEmpty()) {
                    apply(nick);
                    return;
                }
            } catch (IOException e) {
                Moonlight.LOGGER.error("Failed to read launcher nick", e);
            }
        }

        try {
            if (Files.exists(file())) {
                JsonObject json = JsonParser.parseString(Files.readString(file(), StandardCharsets.UTF_8)).getAsJsonObject();

                if (json.has("current"))
                    this.current = json.get("current").getAsString();

                if (json.has("accounts"))
                    json.getAsJsonArray("accounts").forEach(e -> accounts.add(e.getAsString()));
            }
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to read accounts", e);
        }

        if (this.current == null && !accounts.isEmpty())
            this.current = accounts.get(0);

        if (this.current != null)
            apply(this.current);
        else
            apply(MinecraftClient.getInstance().getSession().getUsername());
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("current", current);

        JsonArray array = new JsonArray();
        accounts.forEach(array::add);
        json.add("accounts", array);

        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to save accounts", e);
        }
    }

    public void add(String name) {
        name = name.trim();

        if (name.isEmpty() || accounts.contains(name))
            return;

        accounts.add(name);
        apply(name);
    }

    public void remove(String name) {
        accounts.remove(name);
        save();
    }

    public void apply(String name) {
        MinecraftClient client = MinecraftClient.getInstance();

        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Session session = client.getSession();

        SessionAccessor accessor = (SessionAccessor) session;
        accessor.setUsername(name);
        accessor.setUuid(uuid);

        this.current = name;
        save();
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public String getCurrent() {
        return current == null ? "Player" : current;
    }

}

