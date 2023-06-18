package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.powernukkitx.replaynk.ReplayNK;
import cn.powernukkitx.replaynk.trail.Marker;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public class AddMarkerItem extends ReplayNKItem {
    public AddMarkerItem() {
        super("replaynk:add_marker", "Add Marker", "replaynk_add_marker");
    }

    @Override
    public void onInteract(Player player) {
        if (!Trail.isOperatingTrail(player)) {
            player.sendMessage(ReplayNK.getI18n().tr(player.getLanguageCode(), "replaynk.trail.notoperatingtrail"));
            return;
        }
        var trail = Trail.getOperatingTrail(player);
        var location = player.getLocation();
        var builder = Marker.builder()
                .x((float) location.x)
                .y((float) location.y)
                .z((float) location.z)
                .rotX((float) location.pitch)
                .rotY((float) location.yaw);
        trail.addMarker(builder);
        player.sendMessage(ReplayNK.getI18n().tr(player.getLanguageCode(), "replaynk.mark.added"));
    }
}
