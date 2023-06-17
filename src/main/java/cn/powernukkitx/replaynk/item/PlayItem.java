package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public class PlayItem extends ReplayNKItem {
    public PlayItem() {
        super("replaynk:play", "Play", "replaynk_play");
    }

    @Override
    public void onInteract(Player player) {
        if (Trail.isOperatingTrail(player)) {
            var trail = Trail.getOperatingTrail(player);
            player.sendMessage("§aStart playing trail " + trail.getName());
            trail.play(player);
        }
    }
}
