package moonlight.client.handler;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import moonlight.api.event.EventSubscribe;
import moonlight.api.event.events.EventChat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    private final String prefix = "$";

    @EventSubscribe
    public void onChat(EventChat event) {
        String message = event.getMessage().trim();

        if(!message.startsWith(this.prefix))
            return;

        event.setCanceled(true);

        String[] parts = message.substring(this.prefix.length()).split("\\s+");
        String command = parts[0].toLowerCase();

        switch(command) {
            case "help" -> help();
            case "gps" -> gps();
            case "friend" -> friend(parts);
            default -> send("Неизвестная команда. " + this.prefix + "help — список команд", Formatting.RED);
        }
    }

    private void help() {
        send("Команды Moonlight:", Formatting.AQUA);
        send("  " + this.prefix + "help — список команд", Formatting.GRAY);
        send("  " + this.prefix + "gps — твои координаты", Formatting.GRAY);
        send("  " + this.prefix + "friend add <ник> — добавить друга", Formatting.GRAY);
        send("  " + this.prefix + "friend remove <ник> — удалить друга", Formatting.GRAY);
        send("  " + this.prefix + "friend list — список друзей", Formatting.GRAY);
    }

    private void gps() {
        var client = MinecraftClient.getInstance();

        if(client.player == null || client.world == null) {
            send("Ты не в мире", Formatting.RED);
            return;
        }

        String dimension = client.world.getRegistryKey().getValue().getPath();
        String coords = String.format("X: %d  Y: %d  Z: %d",
                (int) Math.floor(client.player.getX()),
                (int) Math.floor(client.player.getY()),
                (int) Math.floor(client.player.getZ()));

        send("GPS: " + coords + "  [" + dimension + "]", Formatting.AQUA);
    }

    private void friend(String[] parts) {
        if(parts.length < 2) {
            help();
            return;
        }

        switch(parts[1].toLowerCase()) {
            case "add" -> {
                if(parts.length < 3) { send("Используй: $friend add <ник>", Formatting.RED); return; }
                if(addFriend(parts[2])) send("Друг добавлен: " + parts[2], Formatting.GREEN);
                else send("Уже в списке", Formatting.RED);
            }
            case "remove" -> {
                if(parts.length < 3) { send("Используй: $friend remove <ник>", Formatting.RED); return; }
                if(removeFriend(parts[2])) send("Друг удалён: " + parts[2], Formatting.GREEN);
                else send("Не найден в списке", Formatting.RED);
            }
            case "list" -> {
                List<String> friends = loadFriends();

                if(friends.isEmpty()) { send("Список друзей пуст", Formatting.GRAY); return; }

                send("Друзья (" + friends.size() + "):", Formatting.AQUA);
                for(String friend : friends)
                    send("  " + friend, Formatting.GRAY);
            }
            case "clear" -> {
                saveFriends(new ArrayList<>());
                send("Список очищен", Formatting.GREEN);
            }
            default -> send("Используй: $friend <add|remove|list|clear>", Formatting.RED);
        }
    }

    private Path friendsFile() {
        return FabricLoader.getInstance().getGameDir().resolve("moonlight").resolve("friends.txt");
    }

    private List<String> loadFriends() {
        try {
            if(Files.exists(friendsFile()))
                return new ArrayList<>(Files.readAllLines(friendsFile(), StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }

        return new ArrayList<>();
    }

    private boolean addFriend(String name) {
        List<String> friends = loadFriends();

        if(friends.contains(name)) return false;

        friends.add(name);
        saveFriends(friends);
        return true;
    }

    private boolean removeFriend(String name) {
        List<String> friends = loadFriends();

        if(!friends.remove(name)) return false;

        saveFriends(friends);
        return true;
    }

    private void saveFriends(List<String> friends) {
        try {
            Files.createDirectories(friendsFile().getParent());
            Files.write(friendsFile(), friends, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private void send(String message, Formatting formatting) {
        var client = MinecraftClient.getInstance();

        if(client.inGameHud != null)
            client.inGameHud.getChatHud().addMessage(
                    Text.literal(message).formatted(formatting));
    }

}
