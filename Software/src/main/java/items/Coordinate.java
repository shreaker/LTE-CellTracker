package items;

public class Coordinate {

    private final double lat;
    private final double lng;
    private final int radius;

    ////////////////////////////////////////////////////////////////////////////

    public Coordinate(final double lat, final double lng, final int radius) {

        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    ////////////////////////////////////////////////////////////////////////////

    public final double getLatitude() {
        return lat;
    }

    public final double getLongitude() {
        return lng;
    }

    public final int getRadius() {
        return radius;
    }
}
