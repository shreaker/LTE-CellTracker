package items;

import java.util.HashMap;
import java.util.Map;

public class Cell {

    private final int mcc;
    private final int mnc;
    private final int tac;
    private final int cid;
    private Map<String, Coordinate> coordinates = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////

    public Cell(final int mcc, final int mnc, final int tac, final int cid) {

        this.mcc = mcc;
        this.mnc = mnc;
        this.tac = tac;
        this.cid = cid;
    }

    ////////////////////////////////////////////////////////////////////////////

    public final void addCoordinates(final String type, final Coordinate coord) {

        this.coordinates.put(type, coord);
    }

    ////////////////////////////////////////////////////////////////////////////

    public final int getMcc() {
        return mcc;
    }

    public final int getMnc() {
        return mnc;
    }

    public final int getTac() {
        return tac;
    }

    public final int getCid() {
        return cid;
    }

    public final Map<String, Coordinate> getCoordinates() {
        return coordinates;
    }
}
