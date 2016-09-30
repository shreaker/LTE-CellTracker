package locator.google;

import items.Cell;
import items.Coordinate;
import utilities.ConfigException;

import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        /////////////////////////
        //EXAMPLE for CellTracker
        /////////////////////////
        GoogleLocator geoLocGoogleClient = GoogleLocator.getInstance();
        try {
            geoLocGoogleClient.initRestClient("https://www.googleapis.com/geolocation/v1/geolocate?key=", "../../CellTracker.ini", RadioType.LTE);
        } catch (ConfigException ioExceptionConfigIni) {
            System.err.println("Error reading config. ini file");
            ioExceptionConfigIni.printStackTrace();
            System.exit(1);
        }

        Set<Cell> cellList = new HashSet<>();
        cellList.add(new Cell(262, 1, 21009, 26526978));
        cellList.add(new Cell(262, 2, 48035, 20664834));

        try {
            geoLocGoogleClient.insertCellCoords(cellList);
            for (Cell cell : cellList) {
                Coordinate coord = cell.getCoordinates().get(geoLocGoogleClient.getLocatorId());
                System.out.println("Cell info: lat " + coord.getLatitude() + " lon " + coord.getLongitude() + " radius " + coord.getRadius());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
