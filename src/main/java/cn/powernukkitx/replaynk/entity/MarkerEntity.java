package cn.powernukkitx.replaynk.entity;

import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public class MarkerEntity extends ReplayNKEntity {
    private static final CustomEntityDefinition DEF =
            CustomEntityDefinition
                    .builder()
                    .identifier("replaynk:marker")
                    .summonable(true)
                    .spawnEgg(false)
                    .build();
    private static final String MARKER_INDEX_KEY = "MarkerIndex";

    public MarkerEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public CustomEntityDefinition getDefinition() {
        return DEF;
    }

    @Override
    public String getOriginalName() {
        return "Marker";
    }

    public int getMarkerIndex() {
        return namedTag.getInt(MARKER_INDEX_KEY);
    }

    public void setMarkerIndex(int index) {
        namedTag.putInt(MARKER_INDEX_KEY, index);
        setNameTag("§a" + index);
    }
}
