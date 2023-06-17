package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public class AddMarkerItem extends ReplayNKItem {
    public AddMarkerItem() {
        super("replaynk:add_marker", "Marker", "replaynk_add_marker");
    }

    @Override
    public void onInteract(Player player) {
        if (!Trail.isOperatingTrail(player)) {
            player.sendMessage("§cYou are not operating a trail.");
        }
        var trail = Trail.getOperatingTrail(player);
        var location = player.getLocation();
        var builder = Trail.Marker.builder()
                .x((float) location.x)
                .y((float) location.y)
                .z((float) location.z)
                .rotX((float) location.pitch)
                .rotY((float) location.yaw);
        trail.addMarker(builder);
        player.sendMessage("§aMarker added.");
    }
}
