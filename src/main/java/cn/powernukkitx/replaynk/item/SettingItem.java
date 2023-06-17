package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.powernukkitx.replaynk.trail.Trail;

/**
 * @author daoge_cmd
 * @date 2023/6/16
 * ReplayNK Project
 */
public class SettingItem extends ReplayNKItem {
    public SettingItem() {
        super("replaynk:setting", "Setting", "replaynk_setting");
    }

    @Override
    public void onInteract(Player player) {
        if (!Trail.isOperatingTrail(player)) {
            player.sendMessage("§cYou are not operating a trail.");
            return;
        }
        var trail = Trail.getOperatingTrail(player);
        trail.showEditorForm(player);
    }
}
