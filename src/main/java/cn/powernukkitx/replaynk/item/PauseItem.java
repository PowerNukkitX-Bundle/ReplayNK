package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public class PauseItem extends ReplayNKItem {
    public PauseItem() {
        super("replaynk:pause", "Pause", "replaynk_pause");
    }

    @Override
    public void onInteract(Player player) {
        if (Trail.isOperatingTrail(player)) {
            var trail = Trail.getOperatingTrail(player);
            if (trail.pause()) {
                player.sendMessage("§aPlaying paused.");
            } else {
                player.sendMessage("§cTrail is not playing.");
            }
        }
    }
}


