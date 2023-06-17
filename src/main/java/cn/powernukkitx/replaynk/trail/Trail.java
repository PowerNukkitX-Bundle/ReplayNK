package cn.powernukkitx.replaynk.trail;

import cn.nukkit.Player;
import cn.nukkit.api.DoNotModify;
import cn.nukkit.camera.data.*;
import cn.nukkit.camera.instruction.impl.ClearInstruction;
import cn.nukkit.camera.instruction.impl.SetInstruction;
import cn.nukkit.entity.Entity;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.CameraInstructionPacket;
import cn.nukkit.potion.Effect;
import cn.powernukkitx.replaynk.ReplayNK;
import cn.powernukkitx.replaynk.entity.MarkerEntity;
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
import java.util.concurrent.CompletableFuture;

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
    private static final double DEFAULT_BEZIER_CURVE_STEP = 0.001;
    private static final double DEFAULT_MIN_DISTANCE = 0.5;
    private static final double DEFAULT_CAMERA_SPEED = 2;

    private transient Player operator;
    @Setter
    private transient boolean playing;
    private transient List<Marker> runtimeMarkers;
    @Setter
    private transient boolean changed;

    private final List<Marker> markers = new ArrayList<>();
    private final String name;
    @Setter
    private boolean useBezierCurves = false;
    @Setter
    private boolean showBezierCurves = true;
    @Setter
    private double minDistance = DEFAULT_MIN_DISTANCE;
    @Setter
    private double defaultCameraSpeed = DEFAULT_CAMERA_SPEED;

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
        markers.forEach(marker -> marker.spawnDisplayEntity(player.getLevel(), this));
    }

    public void tick() {
        if (operator != null && useBezierCurves && showBezierCurves && !playing) {
            getOrCalculateRuntimeMarkers().forEach(marker -> operator.getLevel().addParticleEffect(new Vector3(marker.getX(), marker.getY(), marker.getZ()), ParticleEffect.BALLOON_GAS));
        }
    }

    public void clearRuntimeMarkers() {
        if (runtimeMarkers == null) {
            runtimeMarkers = new ArrayList<>();
            return;
        }
        if (runtimeMarkers.isEmpty())
            return;
        runtimeMarkers.clear();
    }

    public List<Marker> getOrCalculateRuntimeMarkers() {
        if (runtimeMarkers == null || isChanged()) {
            prepareRuntimeMarkers();
        }
        return runtimeMarkers;
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
        addMarker(marker);
    }

    public void addMarker(Marker marker) {
        markers.add(marker);
        if (operator != null) {
            marker.spawnDisplayEntity(operator.getLevel(), this);
        }
        setChanged(true);
    }

    public void insertMarker(int index, Marker marker) {
        markers.add(index, marker);
        if (operator != null) {
            marker.spawnDisplayEntity(operator.getLevel(), this);
        }
        for (int i = index + 1; i < markers.size(); i++) {
            markers.get(i).updateDisplayEntity(this);
        }
        setChanged(true);
    }

    public void removeMarker(int index) {
        var removedMarker = markers.remove(index);
        removedMarker.deleteDisplayEntity();
        for (int i = index; i < markers.size(); i++) {
            markers.get(i).updateDisplayEntity(this);
        }
        setChanged(true);
    }

    public void moveMarker(int oldIndex, int newIndex) {
        var marker = markers.remove(oldIndex);
        markers.add(newIndex, marker);
        for (int i = Math.min(oldIndex, newIndex); i < markers.size(); i++) {
            markers.get(i).updateDisplayEntity(this);
        }
        setChanged(true);
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
        prepareRuntimeMarkers();
        runtimeMarkers.get(0).play(player, this);
    }

    public void showEditorForm(Player player) {
        var useBezierCurvesElement = new ElementToggle("Use Bezier Curves", useBezierCurves);
        var showBazierCurvesElement = new ElementToggle("Show Bezier Curves", showBezierCurves);
        var minDistanceElement = new ElementInput("Minimum Distance Between Two Points", String.valueOf(DEFAULT_MIN_DISTANCE), String.valueOf(minDistance));
        var cameraSpeedElement = new ElementInput("Default Camera Speed", String.valueOf(DEFAULT_CAMERA_SPEED), String.valueOf(defaultCameraSpeed));
        var doRecalculateEaseTimeElement = new ElementToggle("Recalculate Ease Time?", true);
        var form = new FormWindowCustom(name, List.of(useBezierCurvesElement, showBazierCurvesElement, minDistanceElement, cameraSpeedElement, doRecalculateEaseTimeElement));
        form.addHandler((p, id) -> {
            var response = form.getResponse();
            if (response == null) return;
            try {
                useBezierCurves = response.getToggleResponse(0);
                showBezierCurves = response.getToggleResponse(1);
                minDistance = Double.parseDouble(response.getInputResponse(2));
                defaultCameraSpeed = Double.parseDouble(response.getInputResponse(3));
                if (response.getToggleResponse(4)) {
                    computeAllLinearEaseTime(markers, defaultCameraSpeed, false);
                }
            } catch (Exception e) {
                player.sendMessage("§cInvalid input.");
            }
        });
        player.showFormWindow(form);
    }

    public void prepareRuntimeMarkers() {
        clearRuntimeMarkers();
        if (useBezierCurves) {
            int n = markers.size() - 1;

            for (double u = 0; u <= 1; u += DEFAULT_BEZIER_CURVE_STEP) {
                Marker[] p = new Marker[n + 1];
                for (int i = 0; i <= n; i++) {
                    p[i] = new Marker(markers.get(i));
                }

                for (int r = 1; r <= n; r++) {
                    for (int i = 0; i <= n - r; i++) {
                        p[i].x = (1 - u) * p[i].x + u * p[i + 1].x;
                        p[i].y = (1 - u) * p[i].y + u * p[i + 1].y;
                        p[i].z = (1 - u) * p[i].z + u * p[i + 1].z;
                        p[i].rotX = (1 - u) * p[i].rotX + u * p[i + 1].rotX;
                        p[i].rotY = (1 - u) * p[i].rotY + u * p[i + 1].rotY;
                        //speed
                    }
                }
                runtimeMarkers.add(p[0]);
            }

            computeAllLinearEaseTime(runtimeMarkers, defaultCameraSpeed, true);
        } else {
            runtimeMarkers.addAll(markers);
        }
    }

    private void computeAllLinearEaseTime(List<Marker> markers, double cameraSpeed, boolean doRemoveTooCloseMarker) {
        boolean first = true;
        for (Iterator<Marker> iterator = markers.iterator(); iterator.hasNext(); ) {
            var marker = iterator.next();
            if (first) {
                first = false;
                marker.easeTime = 1;
                continue;
            }
            var lastMarker = markers.get(markers.indexOf(marker) - 1);
            var distance = Math.sqrt(Math.pow(lastMarker.x - marker.x, 2) + Math.pow(lastMarker.y - marker.y, 2) + Math.pow(lastMarker.z - marker.z, 2));
            if (distance < minDistance && doRemoveTooCloseMarker) {
                iterator.remove();
            } else {
                marker.easeTime = distance / cameraSpeed;
            }
        }
    }

    public boolean pause() {
        if (!playing)
            return false;
        playing = false;
        return true;
    }

    @Getter
    public static final class Marker {
        private double x;
        private double y;
        private double z;
        private double rotX;
        private double rotY;
        private EaseType easeType;
        private double easeTime;

        private transient MarkerEntity markerEntity;

        public Marker(double x, double y, double z, double rotX, double rotY, EaseType easeType, double easeTime) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotX = rotX;
            this.rotY = rotY;
            this.easeType = easeType;
            this.easeTime = easeTime;
        }

        public Marker(Marker marker) {
            this.x = marker.x;
            this.y = marker.y;
            this.z = marker.z;
            this.rotX = marker.rotX;
            this.rotY = marker.rotY;
            this.easeType = marker.easeType;
            this.easeTime = marker.easeTime;
        }

        public void computeLinearEaseTime(Marker lastMarker, double cameraSpeed) {
            var distance = Math.sqrt(Math.pow(lastMarker.x - x, 2) + Math.pow(lastMarker.y - y, 2) + Math.pow(lastMarker.z - z, 2));
            this.easeTime = distance / cameraSpeed;
        }

        public static MarkerBuilder builder() {
            return new MarkerBuilder();
        }

        public void spawnDisplayEntity(Level level, Trail trail) {
            if (markerEntity != null)
                throw new IllegalStateException("§cMarker entity already exists.");
            markerEntity = (MarkerEntity) Entity.createEntity("replaynk:marker", new Position(x, y, z, level));
            if (markerEntity == null)
                throw new IllegalStateException("§cFailed to create marker entity.");
            markerEntity.setNameTagAlwaysVisible(true);
            updateDisplayEntity(trail);
            markerEntity.spawnToAll();
        }

        public void respawnDisplayEntity(Level level, Trail trail) {
            markerEntity.close();
            markerEntity = null;
            spawnDisplayEntity(level, trail);
        }

        public void updateDisplayEntity(Trail trail) {
            if (markerEntity != null) {
                var index = trail.getMarkers().indexOf(this);
                markerEntity.setPitch(rotX);
                markerEntity.setYaw(rotY);
                markerEntity.setPosition(new Vector3(x, y, z));
                markerEntity.setMarkerIndex(index);
            }
        }

        public void deleteDisplayEntity() {
            if (markerEntity != null) {
                markerEntity.close();
                markerEntity = null;
            }
        }

        public boolean isDisplayEntitySpawned() {
            return markerEntity != null;
        }

        public void showEditorForm(Player player, Trail trail) {
            var markers = trail.getMarkers();
            var posElement = new ElementInput("Pos", "Enter the new pos", x + "," + y + "," + z);
            var rotElement = new ElementInput("Rot", "Enter the new rot", rotX + "," + rotY);
            var easeTypeElement = new ElementDropdown("EaseType", Arrays.stream(EaseType.values()).map(EaseType::getType).toList(), 0);
            var easeTimeElement = new ElementInput("EaseTime", "Enter the new ease time", String.valueOf(this.easeTime));
            var autoEaseTimeElement = new ElementToggle("Automatically recalculate the ease time if pos changed?", true);
            var indexElement = new ElementInput("Index", "Enter the new index", String.valueOf(markers.indexOf(this)));
            var form = new FormWindowCustom("Marker - " + markers.indexOf(this), List.of(posElement, rotElement, easeTypeElement, easeTimeElement, autoEaseTimeElement, indexElement));
            form.addHandler((p, id) -> {
                var response = form.getResponse();
                if (response == null) return;
                try {
                    var pos = response.getInputResponse(0).split(",");
                    if (pos.length != 3) {
                        player.sendMessage("§cInvalid pos format.");
                        return;
                    }
                    x = Double.parseDouble(pos[0]);
                    y = Double.parseDouble(pos[1]);
                    z = Double.parseDouble(pos[2]);

                    var rot = response.getInputResponse(1).split(",");
                    if (rot.length != 2) {
                        player.sendMessage("§cInvalid rot format.");
                        return;
                    }
                    rotX = Double.parseDouble(rot[0]);
                    rotY = Double.parseDouble(rot[1]);

                    easeType = EaseType.valueOf(response.getDropdownResponse(2).getElementContent().toUpperCase());
                    easeTime = Double.parseDouble(response.getInputResponse(3));

                    var autoEaseTime = response.getToggleResponse(4);
                    if (autoEaseTime) {
                        var index = markers.indexOf(this);
                        if (index > 0) {
                            var lastMarker = markers.get(index - 1);
                            computeLinearEaseTime(lastMarker, trail.getDefaultCameraSpeed());
                        }
                    }

                    var newIndex = Integer.parseInt(response.getInputResponse(5));
                    if (newIndex < 0 || newIndex >= markers.size()) {
                        player.sendMessage("§cInvalid index.");
                        return;
                    }
                    int oldIndex = markers.indexOf(this);
                    if (newIndex != oldIndex) {
                        trail.moveMarker(oldIndex, newIndex);
                    }

                    respawnDisplayEntity(player.getLevel(), trail);
                } catch (Exception e) {
                    player.sendMessage("§cInvalid input.");
                }
            });
            player.showFormWindow(form);
        }

        public void play(Player player, Trail trail) {
            var runtimeMarkers = trail.getRuntimeMarkers();
            var markers = trail.getMarkers();
            var preset = CameraPreset.FREE;
            var pk = new CameraInstructionPacket();
            pk.setInstruction(SetInstruction.builder()
                    .preset(preset)
                    .pos(new Pos((float) x, (float) y, (float) z))
                    .rot(new Rot((float) rotX, (float) rotY))
                    .ease(new Ease((float) easeTime, easeType))
                    .build());
            player.dataPacket(pk);
            CompletableFuture.runAsync(() -> {
                if (runtimeMarkers.indexOf(this) != 0) {
                    try {
                        //提前10ms以避免卡顿
                        Thread.sleep((long) (easeTime * 1000) - 10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!trail.isPlaying() || runtimeMarkers.indexOf(this) == runtimeMarkers.size() - 1) {
                    trail.setPlaying(false);
                    resetCamera(player);
                    markers.forEach(Marker::visible);
                    trail.clearRuntimeMarkers();
                    return;
                }
                var next = runtimeMarkers.get(runtimeMarkers.indexOf(this) + 1);
                next.play(player, trail);
            });
        }

        public void invisible() {
            if (markerEntity != null) {
                markerEntity.addEffect(Effect.getEffect(Effect.INVISIBILITY).setDuration(999999).setVisible(false));
            }
        }

        public void visible() {
            if (markerEntity != null) {
                markerEntity.removeEffect(Effect.INVISIBILITY);
            }
        }

        private void resetCamera(Player player) {
            var pk = new CameraInstructionPacket();
            pk.setInstruction(ClearInstruction.get());
            player.dataPacket(pk);
        }
    }

    public static class MarkerBuilder {
        private double x;
        private double y;
        private double z;
        private double rotX = 0;
        private double rotY = 0;
        private EaseType easeType = EaseType.LINEAR;
        private double easeTime = 1;

        MarkerBuilder() {
        }

        public MarkerBuilder x(double x) {
            this.x = x;
            return this;
        }

        public MarkerBuilder y(double y) {
            this.y = y;
            return this;
        }

        public MarkerBuilder z(double z) {
            this.z = z;
            return this;
        }

        public MarkerBuilder rotX(double rotX) {
            this.rotX = rotX;
            return this;
        }

        public MarkerBuilder rotY(double rotY) {
            this.rotY = rotY;
            return this;
        }

        public MarkerBuilder easeType(EaseType easeType) {
            this.easeType = easeType;
            return this;
        }

        public MarkerBuilder easeTime(double easeTime) {
            this.easeTime = easeTime;
            return this;
        }

        public Marker build(Trail trail) {
            var markers = trail.getMarkers();
            var marker = new Marker(this.x, this.y, this.z, this.rotX, this.rotY, this.easeType, this.easeTime);
            if (markers.size() > 0) {
                var lastMarker = markers.get(markers.size() - 1);
                marker.computeLinearEaseTime(lastMarker, trail.getDefaultCameraSpeed());
            } else {
                marker.easeTime = 1;
            }
            return marker;
        }

        public Marker build(Marker lastMarker, double cameraSpeed) {
            var marker = new Marker(this.x, this.y, this.z, this.rotX, this.rotY, this.easeType, this.easeTime);
            marker.computeLinearEaseTime(lastMarker, cameraSpeed);
            return marker;
        }
    }
}
