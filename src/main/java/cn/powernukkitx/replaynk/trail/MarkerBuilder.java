package cn.powernukkitx.replaynk.trail;

import cn.nukkit.camera.data.EaseType;

/**
 * @author daoge_cmd
 * @date 2023/6/17
 * ReplayNK Project
 */
public class MarkerBuilder {
    private float x;
    private float y;
    private float z;
    private float rotX = 0;
    private float rotY = 0;
    private EaseType easeType = EaseType.LINEAR;
    private float easeTime = -1;

    MarkerBuilder() {
    }

    public MarkerBuilder x(float x) {
        this.x = x;
        return this;
    }

    public MarkerBuilder y(float y) {
        this.y = y;
        return this;
    }

    public MarkerBuilder z(float z) {
        this.z = z;
        return this;
    }

    public MarkerBuilder rotX(float rotX) {
        this.rotX = rotX;
        return this;
    }

    public MarkerBuilder rotY(float rotY) {
        this.rotY = rotY;
        return this;
    }

    public MarkerBuilder easeType(EaseType easeType) {
        this.easeType = easeType;
        return this;
    }

    public MarkerBuilder easeTime(float easeTime) {
        this.easeTime = easeTime;
        return this;
    }

    public Marker build(Trail trail) {
        return new Marker(trail, this.x, this.y, this.z, this.rotX, this.rotY, this.easeType, this.easeTime);
    }
}
