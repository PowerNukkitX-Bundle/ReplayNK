package cn.powernukkitx.replaynk.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public abstract class ReplayNKItem extends ItemCustom {

    public ReplayNKItem(@NotNull String id, @Nullable String name) {
        super(id, name);
    }

    public ReplayNKItem(@NotNull String id, @Nullable String name, @NotNull String textureName) {
        super(id, name, textureName);
    }

    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition
                .simpleBuilder(this, ItemCreativeCategory.NATURE)
                .allowOffHand(false)
                .build();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public void onInteract(Player player) {
        //Do nothing
    }

    public void onClickEntity(Player player, Entity entity) {
        //Do nothing
    }
}
