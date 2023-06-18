package cn.powernukkitx.replaynk.trail;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

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
                    end = new Marker((left.getX() + right.getX()) / 2d, (left.getY() + right.getY()) / 2d, (left.getZ() + right.getZ()) / 2d, (left.getRotX() + right.getRotX()) / 2d, (left.getRotY() + right.getRotY()) / 2d, right.getEaseType(), (left.getCameraSpeed() + right.getCameraSpeed()) / 2d, right.getDistance() / 2d);
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

    public List<Marker> interpolator(List<Marker> markers, double minDistance) {
        throw new UnsupportedOperationException();
    }

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
                    p[i].setRotY((1 - u) * p[i].getRotY() + u * p[i + 1].getRotY());
                    p[i].setCameraSpeed((1 - u) * p[i].getCameraSpeed() + u * p[i + 1].getCameraSpeed());
                }
            }
            runtimeMarkers.add(p[0]);
        }
        return runtimeMarkers;
    }
}
