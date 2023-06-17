package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.powernukkitx.replaynk.entity.MarkerEntity;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/16
 * ReplayNK Project
 */
public class EditMarkerItem extends ReplayNKItem {
    public EditMarkerItem() {
        super("replaynk:edit_marker", "Edit Marker", "replaynk_edit_marker");
    }

    @Override
    public void onClickEntity(Player player, Entity entity) {
        if (Trail.isOperatingTrail(player) && entity instanceof MarkerEntity markerEntity) {
            var trail = Trail.getOperatingTrail(player);
            var index = markerEntity.getMarkerIndex();
            trail.getMarkers().get(index).showEditorForm(player, trail);
        }
    }
}
