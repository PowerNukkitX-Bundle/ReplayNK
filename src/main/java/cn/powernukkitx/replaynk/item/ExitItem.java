package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public class ExitItem extends ReplayNKItem {
    public ExitItem() {
        super("replaynk:exit", "Exit", "replaynk_exit");
    }

    @Override
    public void onInteract(Player player) {
        if (!Trail.isOperatingTrail(player)) {
            player.sendMessage("§cYou are not operating a trail.");
            return;
        }
        var trail = Trail.getOperatingTrail(player);
        trail.stopOperating();
    }
}
