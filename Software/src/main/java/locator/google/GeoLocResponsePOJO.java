package locator.google;

import items.Coordinate;

import java.io.Serializable;

public class GeoLocResponsePOJO implements Serializable {

    private Location location;
    private float accuracy;

    ////////////////////////////////////////////////////////////////////////////

    public Location getLocation() {
        return location;
    }

    ////////////////////////////////////////////////////////////////////////////

    public float getAccuracy() {
        return accuracy;
    }

    ////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "{\n" +
                " \"location\": {\n" +
                "  \"lat\":" + location.getLatitude() + ",\n" +
                "  \"lng\":" + location.getLongitude() + "\n" +
                " },\n" +
                " \"accuracy\":" + accuracy + "\n" +
                "}";
    }

    //////////////
    //Inner class
    /////////////
    public class Location {

        private double lat;
        private double lng;

        public double getLongitude() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getLatitude() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }
    }
}
