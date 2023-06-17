package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.powernukkitx.replaynk.entity.MarkerEntity;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public class ClearMarkerItem extends ReplayNKItem {
    public ClearMarkerItem() {
        super("replaynk:clear_marker", "Clear Marker", "replaynk_clear_marker");
    }

    @Override
    public void onClickEntity(Player player, Entity entity) {
        if (Trail.isOperatingTrail(player) && entity instanceof MarkerEntity markerEntity) {
            var trail = Trail.getOperatingTrail(player);
            var index = markerEntity.getMarkerIndex();
            trail.removeMarker(index);
            player.sendMessage("§aMarker removed.");
        }
    }
}
