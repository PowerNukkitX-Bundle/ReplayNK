package cn.powernukkitx.replaynk.trail;

import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.math.BVector3;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author daoge_cmd
 * @date 2023/6/18
 * ReplayNK Project
 */
public enum Interpolator {
    LINEAR {
        @Override
        public List<Marker> interpolator(List<Marker> markers, double minDistance) {
            var cloned = new ArrayList<Marker>();
            for (var marker : markers) {
                cloned.add(new Marker(marker));
            }
            return cloned;
        }

        @Override
        public void showParticle(List<Marker> markers, Level level, boolean showDirection) {
            for (int i = 0; i < markers.size() - 1; i++) {
                var startMarker = markers.get(i);
                var startVec = startMarker.getVector3();
                var endMarker = markers.get(i + 1);
                var endVec = endMarker.getVector3();
                var distance = (int) startVec.distance(endVec);
                for (double j = 0; j < distance; j += 0.5) {
                    var vec = startVec.add(endVec.subtract(startVec).multiply(j / distance));
                    level.addParticleEffect(vec, ParticleEffect.BALLOON_GAS);
                    if (showDirection) {
                        var rotX = startMarker.getRotX() + (endMarker.getRotX() - startMarker.getRotX()) * j / distance;
                        var rotY = startMarker.getRotY() + (endMarker.getRotY() - startMarker.getRotY()) * j / distance;
                        Marker.spawnDirectionParticle(vec, rotX, rotY, level);
                    }
                }
            }
        }
    },
    BEZIER_CURVES {
        @Override
        public List<Marker> interpolator(List<Marker> markers, double minDistance) {
            var runtimeMarkers = Interpolator.bezier(markers, minDistance);
            Trail.computeAllLinearDistance(runtimeMarkers, true, minDistance);
            return runtimeMarkers;
        }
    },
    SEGMENTED_BEZIER_CURVES {
        @Override
        public List<Marker> interpolator(List<Marker> markers, double minDistance) {
            if (markers.size() <= 4)
                return BEZIER_CURVES.interpolator(markers, minDistance);
            var runtimeMarkers = new ArrayList<Marker>();
            var start = markers.get(0);
            int i = 1;
            do {
                i += 2;
                Marker end;
                if (i < markers.size() - 1) {
                    var left = markers.get(i - 1);
                    var right = markers.get(i);
                    end = new Marker((left.getX() + right.getX()) / 2d, (left.getY() + right.getY()) / 2d, (left.getZ() + right.getZ()) / 2d, (left.getRotX() + right.getRotX()) / 2d, (left.getRotY() + right.getRotY()) / 2d, right.getEaseType(), (left.getCameraSpeed() + right.getCameraSpeed()) / 2d);
                } else {
                    end = markers.get(markers.size() - 1);
                }
                if (!runtimeMarkers.isEmpty())
                    runtimeMarkers.remove(runtimeMarkers.size() - 1);
                runtimeMarkers.addAll(Interpolator.bezier(List.of(start, markers.get(i - 2), markers.get(i - 1), end), minDistance));
                start = end;
            } while (i < markers.size() - 1);
            Trail.computeAllLinearDistance(runtimeMarkers, true, minDistance);
            return runtimeMarkers;
        }
    };

    public static final List<String> INTERPOLATOR_NAMES = Arrays.stream(values()).map(interpolator -> interpolator.name().toLowerCase()).toList();
    private static final double DEFAULT_BEZIER_CURVE_STEP = 0.001;

    private static List<Marker> bezier(List<Marker> markers, double minDistance) {
        if (markers.size() <= 1)
            return LINEAR.interpolator(markers, minDistance);
        var runtimeMarkers = new ArrayList<Marker>();
        int n = markers.size() - 1;

        for (double u = 0; u <= 1; u += DEFAULT_BEZIER_CURVE_STEP) {
            Marker[] p = new Marker[n + 1];
            for (int i = 0; i <= n; i++) {
                p[i] = new Marker(markers.get(i));
            }

            for (int r = 1; r <= n; r++) {
                for (int i = 0; i <= n - r; i++) {
                    p[i].setX((1 - u) * p[i].getX() + u * p[i + 1].getX());
                    p[i].setY((1 - u) * p[i].getY() + u * p[i + 1].getY());
                    p[i].setZ((1 - u) * p[i].getZ() + u * p[i + 1].getZ());
                    p[i].setRotX((1 - u) * p[i].getRotX() + u * p[i + 1].getRotX());
                    var startDirection = p[i].getDirectionVec().addToPos();
                    var endDirection = p[i + 1].getDirectionVec().addToPos();
                    var resultDirection = BVector3.fromPos(
                            (1 - u) * startDirection.getX() + u * endDirection.getX(),
                            (1 - u) * startDirection.getY() + u * endDirection.getY(),
                            (1 - u) * startDirection.getZ() + u * endDirection.getZ());
                    p[i].setRotY(resultDirection.getYaw());
                    p[i].setCameraSpeed((1 - u) * p[i].getCameraSpeed() + u * p[i + 1].getCameraSpeed());
                }
            }
            runtimeMarkers.add(p[0]);
        }
        return runtimeMarkers;
    }

    /**
     * 调用此方法即已保证markers.size() >= 2
     */
    public List<Marker> interpolator(List<Marker> markers, double minDistance) {
        throw new UnsupportedOperationException();
    }

    public void showParticle(List<Marker> markers, Level level, boolean showDirection) {
        markers.forEach(marker -> {
            level.addParticleEffect(new Vector3(marker.getX(), marker.getY(), marker.getZ()), ParticleEffect.BALLOON_GAS);
            if (showDirection)
                Marker.spawnDirectionParticle(marker.getVector3(), marker.getRotX(), marker.getRotY(), level);
        });
    }
}
