package cn.powernukkitx.replaynk.trail;

import cn.nukkit.Player;
import cn.nukkit.api.DoNotModify;
import cn.nukkit.item.Item;
import cn.powernukkitx.replaynk.ReplayNK;
import cn.powernukkitx.replaynk.item.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @author daoge_cmd
 * @date 2023/6/16
 * ReplayNK Project
 */
@Getter
@Log4j2
public final class Trail {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private static final Map<String, Trail> TRAILS = new HashMap<>();
    private static final Map<Player, Trail> OPERATING_TRAILS = new HashMap<>();
    private final List<Marker> markers = new ArrayList<>();
    private final String name;
    private transient Player operator;
    @Setter
    private transient boolean playing;

    private Trail(String name) {
        this.name = name;
    }

    @DoNotModify
    public static Map<String, Trail> getTrails() {
        return TRAILS;
    }

    public static Trail getTrail(String name) {
        return TRAILS.get(name);
    }

    public static void addTrail(Trail trail) {
        if (TRAILS.containsKey(trail.getName()))
            throw new IllegalArgumentException("Trail " + trail.getName() + " already exists.");
        TRAILS.put(trail.getName(), trail);
    }

    public static Trail removeTrail(String name) {
        return TRAILS.remove(name);
    }

    @SneakyThrows
    public static void readAllTrails() {
        var basePath = ReplayNK.getInstance().getDataFolder().toPath().resolve("trails");
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
            return;
        }
        try (var paths = Files.walk(basePath)) {
            paths.forEach(path -> {
                if (Files.isDirectory(path))
                    return;
                String json;
                try {
                    json = Files.readString(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                var trail = fromJson(json);
                log.info("Loaded trail " + trail.getName() + " from " + path);
            });
        }
    }

    @SneakyThrows
    public static void saveAllTrails() {
        for (var trail : TRAILS.values()) {
            var basePath = ReplayNK.getInstance().getDataFolder().toPath().resolve("trails");
            if (!Files.exists(basePath))
                Files.createDirectory(basePath);
            var path = basePath.resolve(trail.getName() + ".json");
            log.info("Saving trail " + trail.getName() + " to " + path);
            if (!Files.exists(path))
                Files.createFile(path);
            var json = trail.toJson();
            Files.writeString(path, json);
        }
    }

    public static void closeAndSave() {
        for (var trail : OPERATING_TRAILS.values()) {
            trail.stopOperating();
        }
        saveAllTrails();
    }

    public static Trail getOperatingTrail(Player player) {
        return OPERATING_TRAILS.get(player);
    }

    public static boolean isOperatingTrail(Player player) {
        return OPERATING_TRAILS.containsKey(player);
    }

    public static Trail create(String name) {
        if (TRAILS.containsKey(name))
            return null;
        var trail = new Trail(name);
        addTrail(trail);
        return trail;
    }

    public static Trail fromJson(String json) {
        var trail = GSON.fromJson(json, Trail.class);
        addTrail(trail);
        return trail;
    }

    public void startOperating(Player player) {
        if (operator != null)
            throw new IllegalStateException("Trail " + name + " is already operating by " + operator.getName());
        operator = player;
        OPERATING_TRAILS.put(player, this);
        prepareHotBar(player);
        markers.forEach(marker -> marker.spawnDisplayEntity(player.getLevel()));
    }

    private void prepareHotBar(Player player) {
        var inventory = player.getInventory();
        inventory.clearAll();

        var addMarkerItem = new AddMarkerItem();
        var clearMarkerItem = new ClearMarkerItem();
        var editMarkerItem = new EditMarkerItem();
        var playItem = new PlayItem();
        var pauseItem = new PauseItem();
        var settingItem = new SettingItem();
        var exitItem = new ExitItem();

        addMarkerItem.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        clearMarkerItem.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        editMarkerItem.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        playItem.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        pauseItem.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        settingItem.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        exitItem.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);

        inventory.setItem(0, addMarkerItem);
        inventory.setItem(1, clearMarkerItem);
        inventory.setItem(2, editMarkerItem);
        inventory.setItem(3, playItem);
        inventory.setItem(4, pauseItem);
        inventory.setItem(5, settingItem);
        inventory.setItem(8, exitItem);
    }

    public void stopOperating() {
        if (operator == null)
            throw new IllegalStateException("Trail " + name + " is not operating.");
        playing = false;
        OPERATING_TRAILS.remove(operator);
        operator.getInventory().clearAll();
        markers.forEach(Marker::deleteDisplayEntity);
        operator.sendMessage("§aTrail " + name + " stopped operating.");
        operator = null;
    }

    public void addMarker(MarkerBuilder builder) {
        var marker = builder.build(this);
        markers.add(marker);
        if (operator != null) {
            marker.spawnDisplayEntity(operator.getLevel());
        }
    }

    public void insertMarker(int index, Marker marker) {
        markers.add(index, marker);
        if (operator != null) {
            marker.spawnDisplayEntity(operator.getLevel());
        }
        for (int i = index + 1; i < markers.size(); i++) {
            markers.get(i).updateDisplayEntity();
        }
    }

    public void removeMarker(int index) {
        var removedMarker = markers.remove(index);
        removedMarker.deleteDisplayEntity();
        for (int i = index; i < markers.size(); i++) {
            markers.get(i).updateDisplayEntity();
        }
    }

    public void moveMarker(int oldIndex, int newIndex) {
        var marker = markers.remove(oldIndex);
        markers.add(newIndex, marker);
        for (int i = Math.min(oldIndex, newIndex); i < markers.size(); i++) {
            markers.get(i).updateDisplayEntity();
        }
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public void play(Player player) {
        if (playing) {
            player.sendMessage("§cTrail " + name + " is already playing.");
            return;
        }
        if (markers.size() <= 1) {
            player.sendMessage("§cTrail " + name + " has too little markers.");
            return;
        }
        player.sendMessage("§aTrail " + name + " started playing.");
        playing = true;
        markers.forEach(Marker::invisible);
        markers.get(0).play(player);
    }

    public boolean pause() {
        if (!playing)
            return false;
        playing = false;
        return true;
    }
}
