package cn.powernukkitx.replaynk.trail;

import cn.nukkit.Player;
import cn.nukkit.camera.data.*;
import cn.nukkit.camera.instruction.impl.ClearInstruction;
import cn.nukkit.camera.instruction.impl.SetInstruction;
import cn.nukkit.entity.Entity;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.CameraInstructionPacket;
import cn.nukkit.potion.Effect;
import cn.powernukkitx.replaynk.ReplayNK;
import cn.powernukkitx.replaynk.entity.MarkerEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

/**
 * @author daoge_cmd
 * @date 2023/6/18
 * ReplayNK Project
 */
public final class Marker {
    @Getter
    @Setter
    private double x;
    @Getter
    @Setter
    private double y;
    @Getter
    @Setter
    private double z;
    @Getter
    @Setter
    private double rotX;
    @Getter
    @Setter
    private double rotY;
    @Getter
    @Setter
    private EaseType easeType;
    @Getter
    private double cameraSpeed;
    @Getter
    private double distance;

    private transient double easeTime = -1;
    private transient MarkerEntity markerEntity;
    //用于给RuntimeMark缓存index，防止运镜卡顿
    private transient int cachedIndex;

    public Marker(double x, double y, double z, double rotX, double rotY, EaseType easeType, double cameraSpeed, double distance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotX = rotX;
        this.rotY = rotY;
        this.easeType = easeType;
        this.cameraSpeed = cameraSpeed;
        this.distance = distance;
        computeEaseTime();
    }

    public Marker(double x, double y, double z, double rotX, double rotY, EaseType easeType, double cameraSpeed, Marker lastMarker) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotX = rotX;
        this.rotY = rotY;
        this.easeType = easeType;
        this.cameraSpeed = cameraSpeed;
        computeDistance(lastMarker);
    }

    public Marker(Marker marker) {
        this.x = marker.x;
        this.y = marker.y;
        this.z = marker.z;
        this.rotX = marker.rotX;
        this.rotY = marker.rotY;
        this.easeType = marker.easeType;
        this.cameraSpeed = marker.cameraSpeed;
        this.distance = marker.distance;
        computeEaseTime();
    }

    public static MarkerBuilder builder() {
        return new MarkerBuilder();
    }

    public void cacheIndex(int cachedIndex) {
        this.cachedIndex = cachedIndex;
    }

    public void setCameraSpeedAndCalDistance(Marker lastMarker, double cameraSpeed) {
        setCameraSpeed(cameraSpeed);
        computeDistance(lastMarker);
        computeEaseTime();
    }

    public void computeDistance(Trail trail) {
        var index = trail.getMarkers().indexOf(this);
        if (index == 0) {
            distance = 1;
            cameraSpeed = 1;
        } else {
            computeDistance(trail.getMarkers().get(index - 1));
        }
    }

    public void computeDistance(Marker lastMarker) {
        distance = Math.sqrt(Math.pow(lastMarker.x - x, 2) + Math.pow(lastMarker.y - y, 2) + Math.pow(lastMarker.z - z, 2));
        computeEaseTime();
    }

    public void setDistance(double distance) {
        this.distance = distance;
        computeEaseTime();
    }

    public void setCameraSpeed(double cameraSpeed) {
        this.cameraSpeed = cameraSpeed;
        computeEaseTime();
    }

    public void computeEaseTime() {
        this.easeTime = distance / this.cameraSpeed;
    }

    public double getEaseTime() {
        return easeTime;
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
        var langCode = player.getLanguageCode();
        var markers = trail.getMarkers();
        var posElement = new ElementInput(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.pos"), "", x + ", " + y + ", " + z);
        var rotElement = new ElementInput(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.rot"), "", rotX + ", " + rotY);
        var easeTypeElement = new ElementDropdown(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.easetype"), Arrays.stream(EaseType.values()).map(EaseType::getType).toList(), 0);
        var easeTypeDetailsElement = new ElementLabel(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.easetype.details"));
        var indexElement = new ElementInput(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.index"), "", String.valueOf(markers.indexOf(this)));
        var indexDetailsElement = new ElementLabel(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.index.details"));
        var easeTimeElement = new ElementInput(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.easetime"), "", this.cameraSpeed + "m/s");
        var easeTimeDetailsElement = new ElementLabel(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.easetime.details"));
        var autoEaseTimeElement = new ElementToggle(ReplayNK.getI18n().tr(langCode, "replaynk.mark.editorform.autoeasetime"), true);
        var form = new FormWindowCustom("Marker - " + markers.indexOf(this), List.of(posElement, rotElement, easeTypeElement, easeTypeDetailsElement, indexElement, indexDetailsElement, easeTimeElement, easeTimeDetailsElement, autoEaseTimeElement));
        form.addHandler((p, id) -> {
            var response = form.getResponse();
            if (response == null) return;
            try {
                var pos = response.getInputResponse(0).split(",");
                if (pos.length != 3) {
                    player.sendMessage(ReplayNK.getI18n().tr(langCode, "replaynk.mark.invalidpos"));
                    return;
                }
                x = Double.parseDouble(pos[0]);
                y = Double.parseDouble(pos[1]);
                z = Double.parseDouble(pos[2]);

                var rot = response.getInputResponse(1).split(",");
                if (rot.length != 2) {
                    player.sendMessage(ReplayNK.getI18n().tr(langCode, "replaynk.mark.invalidrot"));
                    return;
                }
                rotX = Double.parseDouble(rot[0]);
                rotY = Double.parseDouble(rot[1]);

                easeType = EaseType.valueOf(response.getDropdownResponse(2).getElementContent().toUpperCase());

                var newIndex = Integer.parseInt(response.getInputResponse(4));
                if (newIndex < 0 || newIndex >= markers.size()) {
                    player.sendMessage(ReplayNK.getI18n().tr(langCode, "replaynk.mark.invalidindex"));
                    return;
                }
                int oldIndex = markers.indexOf(this);
                if (newIndex != oldIndex) {
                    trail.moveMarker(oldIndex, newIndex);
                    computeDistance(trail);
                }
                respawnDisplayEntity(player.getLevel(), trail);

                var easeTimeOrSpeed = response.getInputResponse(6);
                if (easeTimeOrSpeed.endsWith("m/s")) {
                    cameraSpeed = Double.parseDouble(easeTimeOrSpeed.substring(0, easeTimeOrSpeed.length() - 3));
                } else if (easeTimeOrSpeed.endsWith("s")) {
                    var time = Double.parseDouble(easeTimeOrSpeed.substring(0, easeTimeOrSpeed.length() - 1));
                    cameraSpeed = distance / time;
                } else {
                    player.sendMessage(ReplayNK.getI18n().tr(langCode, "replaynk.mark.invalideasetime"));
                    return;
                }

                var autoEaseTime = response.getToggleResponse(8);
                if (autoEaseTime) {
                    var index = markers.indexOf(this);
                    if (index > 0) {
                        var lastMarker = markers.get(index - 1);
                        computeDistance(lastMarker);
                    } else {
                        easeTime = 0;
                    }
                }

                trail.setChanged(true);
            } catch (Exception e) {
                player.sendMessage(ReplayNK.getI18n().tr(langCode, "replaynk.generic.invalidinput"));
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
        if (runtimeMarkers.indexOf(this) != 0) {
            try {
                //提前25ms以避免卡顿
                Thread.sleep((long) (easeTime * 1000) - 25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!trail.isPlaying() || cachedIndex == runtimeMarkers.size() - 1) {
            trail.setPlaying(false);
            resetCamera(player);
            markers.forEach(Marker::visible);
            trail.clearRuntimeMarkers();
            return;
        }
        var next = runtimeMarkers.get(cachedIndex + 1);
        next.play(player, trail);
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
