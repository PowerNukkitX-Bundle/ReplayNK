package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.powernukkitx.replaynk.ReplayNK;
import cn.powernukkitx.replaynk.entity.MarkerEntity;
import cn.powernukkitx.replaynk.trail.Marker;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/22
 * ReplayNK Project
 */
public class MarkerPickerItem extends ReplayNKItem {

    private static final String MARKER_INDEX_KEY = "MarkerIndex";

    public MarkerPickerItem() {
        super("replaynk:marker_picker", "Marker Picker", "replaynk_marker_picker");
    }

    @Override
    public void onClickEntity(Player player, Entity entity) {
        if (!Trail.isOperatingTrail(player)) {
            player.sendMessage(ReplayNK.getI18n().tr(player.getLanguageCode(), "replaynk.trail.notoperatingtrail"));
            return;
        }
        if (entity instanceof MarkerEntity markerEntity) {
            var index = markerEntity.getMarkerIndex();
            setNamedTag(getNamedTag().putInt(MARKER_INDEX_KEY, index));
            player.getInventory().setItemInHand(this);
            player.sendMessage(ReplayNK.getI18n().tr(player.getLanguageCode(), "replaynk.markerpicker.pick.success", index));
        }
    }

    @Override
    public void onInteract(Player player) {
        var trail = Trail.getOperatingTrail(player);
        if (trail == null) {
            player.sendMessage(ReplayNK.getI18n().tr(player.getLanguageCode(), "replaynk.trail.notoperatingtrail"));
            return;
        }
        var index = getHoldingMarkerIndex();
        if (index == -1) {
            player.sendMessage(ReplayNK.getI18n().tr(player.getLanguageCode(), "replaynk.markerpicker.nopickedmarker"));
            return;
        }
        setNamedTag(getNamedTag().remove(MARKER_INDEX_KEY));
        player.getInventory().setItemInHand(this);
        var marker = trail.getMarkers().get(index);
        marker.setX(player.getX());
        marker.setY(player.getY());
        marker.setZ(player.getZ());
        marker.setRotX(player.getPitch());
        marker.setRotY(player.getYaw());
        trail.recalculateLinearDistanceAt(index);
        marker.updateDisplayEntity(trail);
        trail.setChanged(true);
        player.sendMessage(ReplayNK.getI18n().tr(player.getLanguageCode(), "replaynk.markerpicker.move.success", index));
    }

    public int getHoldingMarkerIndex() {
        return getNamedTag().contains(MARKER_INDEX_KEY) ? getNamedTag().getInt(MARKER_INDEX_KEY) : -1;
    }
}
