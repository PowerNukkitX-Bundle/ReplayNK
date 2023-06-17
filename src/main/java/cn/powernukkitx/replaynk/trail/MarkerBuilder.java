package cn.powernukkitx.replaynk.trail;

import cn.nukkit.camera.data.EaseType;

/**
 * @author daoge_cmd
 * @date 2023/6/18
 * ReplayNK Project
 */
public class MarkerBuilder {
    private double x;
    private double y;
    private double z;
    private double rotX = 0;
    private double rotY = 0;
    private EaseType easeType = EaseType.LINEAR;
    private double cameraSpeed = Trail.DEFAULT_CAMERA_SPEED;

    MarkerBuilder() {
    }

    public MarkerBuilder x(double x) {
        this.x = x;
        return this;
    }

    public MarkerBuilder y(double y) {
        this.y = y;
        return this;
    }

    public MarkerBuilder z(double z) {
        this.z = z;
        return this;
    }

    public MarkerBuilder rotX(double rotX) {
        this.rotX = rotX;
        return this;
    }

    public MarkerBuilder rotY(double rotY) {
        this.rotY = rotY;
        return this;
    }

    public MarkerBuilder easeType(EaseType easeType) {
        this.easeType = easeType;
        return this;
    }

    public MarkerBuilder cameraSpeed(double cameraSpeed) {
        this.cameraSpeed = cameraSpeed;
        return this;
    }

    public Marker build(Trail trail) {
        var markers = trail.getMarkers();
        Marker marker;
        if (markers.size() > 0) {
            var lastMarker = markers.get(markers.size() - 1);
            marker = new Marker(this.x, this.y, this.z, this.rotX, this.rotY, this.easeType, this.cameraSpeed, lastMarker);
        } else {
            //花费一秒移动镜头到起始点
            marker = new Marker(this.x, this.y, this.z, this.rotX, this.rotY, this.easeType, 1, 1);
        }
        return marker;
    }

    public Marker build(Marker lastMarker) {
        var marker = new Marker(this.x, this.y, this.z, this.rotX, this.rotY, this.easeType, this.cameraSpeed, lastMarker);
        marker.setCameraSpeedAndCalDistance(lastMarker, this.cameraSpeed);
        return marker;
    }
}
