package cn.powernukkitx.replaynk.trail;

import cn.nukkit.Player;
import cn.nukkit.camera.data.*;
import cn.nukkit.camera.instruction.impl.ClearInstruction;
import cn.nukkit.camera.instruction.impl.SetInstruction;
import cn.nukkit.entity.Entity;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.CameraInstructionPacket;
import cn.nukkit.potion.Effect;
import cn.powernukkitx.replaynk.entity.MarkerEntity;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author daoge_cmd
 * @date 2023/6/17
 * ReplayNK Project
 */
@Getter
public final class Marker {
    private float x;
    private float y;
    private float z;
    private float rotX;
    private float rotY;
    private EaseType easeType;
    private float easeTime;

    private transient final Trail trail;
    private transient MarkerEntity markerEntity;

    Marker(Trail trail, float x, float y, float z, float rotX, float rotY, EaseType easeType, float easeTime) {
        this.trail = trail;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotX = rotX;
        this.rotY = rotY;
        this.easeType = easeType;
        if (easeTime < 0) {
            if (trail.getMarkers().size() > 0) {
                var lastMarker = trail.getMarkers().get(trail.getMarkers().size() - 1);
                var distance = Math.sqrt(Math.pow(lastMarker.x - x, 2) + Math.pow(lastMarker.y - y, 2) + Math.pow(lastMarker.z - z, 2));
                this.easeTime = (float) (distance / 5);
            } else {
                this.easeTime = 0;
            }
        } else {
            this.easeTime = easeTime;
        }
    }

    public static MarkerBuilder builder() {
        return new MarkerBuilder();
    }

    public void spawnDisplayEntity(Level level) {
        if (markerEntity != null)
            throw new IllegalStateException("§cMarker entity already exists.");
        markerEntity = (MarkerEntity) Entity.createEntity("replaynk:marker", new Position(x, y, z, level));
        if (markerEntity == null)
            throw new IllegalStateException("§cFailed to create marker entity.");
        markerEntity.setNameTagAlwaysVisible(true);
        updateDisplayEntity();
        markerEntity.spawnToAll();
    }

    public void respawnDisplayEntity(Level level) {
        markerEntity.close();
        markerEntity = null;
        spawnDisplayEntity(level);
    }

    public void updateDisplayEntity() {
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

    public void showEditorForm(Player player) {
        var posElement = new ElementInput("Pos", "Enter the new pos", x + "," + y + "," + z);
        var rotElement = new ElementInput("Rot", "Enter the new rot", rotX + "," + rotY);
        var easeTypeElement = new ElementDropdown("EaseType", Arrays.stream(EaseType.values()).map(EaseType::getType).toList(), 0);
        var easeTimeElement = new ElementInput("EaseTime", "Enter the new ease time", String.valueOf(this.easeTime));
        var indexElement = new ElementInput("Index", "Enter the new index", String.valueOf(trail.getMarkers().indexOf(this)));
        var form = new FormWindowCustom("Marker - " + trail.getMarkers().indexOf(this), List.of(posElement, rotElement, easeTypeElement, easeTimeElement, indexElement));
        form.addHandler((p, id) -> {
            var response = form.getResponse();
            if (response == null) return;
            try {
                var pos = response.getInputResponse(0).split(",");
                if (pos.length != 3) {
                    player.sendMessage("§cInvalid pos format.");
                    return;
                }
                x = Float.parseFloat(pos[0]);
                y = Float.parseFloat(pos[1]);
                z = Float.parseFloat(pos[2]);

                var rot = response.getInputResponse(1).split(",");
                if (rot.length != 2) {
                    player.sendMessage("§cInvalid rot format.");
                    return;
                }
                rotX = Float.parseFloat(rot[0]);
                rotY = Float.parseFloat(rot[1]);

                easeType = EaseType.valueOf(response.getDropdownResponse(2).getElementContent().toUpperCase());
                easeTime = Float.parseFloat(response.getInputResponse(3));

                var newIndex = Integer.parseInt(response.getInputResponse(4));
                if (newIndex < 0 || newIndex >= trail.getMarkers().size()) {
                    player.sendMessage("§cInvalid index.");
                    return;
                }
                int oldIndex = trail.getMarkers().indexOf(this);
                if (newIndex != oldIndex) {
                    trail.moveMarker(oldIndex, newIndex);
                }

                respawnDisplayEntity(player.getLevel());
            } catch (Exception e) {
                player.sendMessage("§cInvalid input.");
            }
        });
        player.showFormWindow(form);
    }

    public void play(Player player) {
        var preset = CameraPreset.FREE;
        var pk = new CameraInstructionPacket();
        pk.setInstruction(SetInstruction.builder()
                .preset(preset)
                .pos(new Pos(x, y, z))
                .rot(new Rot(rotX, rotY))
                .ease(new Ease(easeTime, EaseType.LINEAR))
                .build());
        player.dataPacket(pk);
        CompletableFuture.runAsync(() -> {
            if (trail.getMarkers().indexOf(this) != 0) {
                try {
                    Thread.sleep((long) (easeTime * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!trail.isPlaying() || trail.getMarkers().indexOf(this) == trail.getMarkers().size() - 1) {
                trail.setPlaying(false);
                resetCamera(player);
                trail.getMarkers().forEach(Marker::visible);
                return;
            }
            var next = trail.getMarkers().get(trail.getMarkers().indexOf(this) + 1);
            next.play(player);
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
