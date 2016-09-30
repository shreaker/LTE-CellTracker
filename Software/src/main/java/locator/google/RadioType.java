package locator.google;

/**
 * The mobile radio type for Google Maps Geolocation API.
 */
public enum RadioType {

    LTE("lte"),
    GSM("gsm"),
    CDMA("cdma"),
    WCDMA("wcdma");

    private final String string;

    private RadioType(final String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
