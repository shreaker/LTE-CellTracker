import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import java.io.*;
import items.Cell;
import items.Coordinate;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import locator.Locator;
import locator.LocatorFactory;
import netscape.javascript.JSObject;
import org.apache.commons.cli.*;
import parser.Parser;
import parser.ParserFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main extends Application implements MapComponentInitializedListener {

    private GoogleMapView mapView;
    private Set<Cell> cellSet = new HashSet<>();
    private String val_locator;

    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void start(final Stage stage) throws Exception {

        mapView = new GoogleMapView();
        mapView.addMapInializedListener(this);

        Scene scene = new Scene(mapView);
        stage.setTitle("CellTracker");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();

        ////////////////////////////////////////////////////////////////////////

        Options options = new Options();

        Option opt_parser = new Option("p", "parser", true, "parser to extract cell info");
        opt_parser.setRequired(true);
        options.addOption(opt_parser);

        Option opt_file = new Option("f", "file", true, "file or folder to parse");
        opt_file.setRequired(true);
        options.addOption(opt_file);

        Option opt_locator = new Option("l", "locator", true, "locator to extract coordinates");
        opt_locator.setRequired(true);
        options.addOption(opt_locator);

        CommandLineParser cmdParser = new DefaultParser();
        CommandLine cmd = cmdParser.parse(
                options,
                getParameters().getRaw().toArray(
                        new String[getParameters().getRaw().size()]
                )
        );

        String val_parser = cmd.getOptionValue("p");
        String val_file = cmd.getOptionValue("f");
        val_locator = cmd.getOptionValue("l");

        ////////////////////////////////////////////////////////////////////////

        ParserFactory parserFactory = new ParserFactory();
        Parser parser = parserFactory.getParser(val_parser);
        parser.getCells(val_file, cellSet);

        ////////////////////////////////////////////////////////////////////////

        LocatorFactory locatorFactory = new LocatorFactory();
        Locator locator = locatorFactory.getLocator(val_locator);
        locator.insertCellCoords(cellSet);
    }

    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void mapInitialized() {

        MapOptions options = new MapOptions();
        options.center(new LatLong(48.137154, 11.576124)) //Munich,Germany
                .zoom(13)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(true)
                .mapType(MapTypeIdEnum.ROADMAP);

        GoogleMap map = mapView.createMap(options);

        ////////////////////////////////////////////////////////////////////////

        Map<Marker, Boolean> markerMap = new HashMap<>();

        for (Cell cell : cellSet) {

            Coordinate coordinate = cell.getCoordinates().get(val_locator);

            if (coordinate == null) {
                continue;
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLong(coordinate.getLatitude(), coordinate.getLongitude()));
            String carrier = getCarrier(cell.getMnc(), markerOptions);
            Marker marker = new Marker(markerOptions);
            map.addMarker(marker);
            markerMap.put(marker, false);

            InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
            infoWindowOptions.content("<h2>" + carrier + "</h2>"
                    + "MCC: " + cell.getMcc() + "<br />"
                    + "MNC: " + cell.getMnc() + "<br />"
                    + "Cell ID: " + cell.getCid() + "<br>"
                    + "TAC: " + cell.getTac() + "<br />"
                    + "Lat: " + coordinate.getLatitude() + "<br />"
                    + "Lon: " + coordinate.getLongitude());
            InfoWindow infoWindow = new InfoWindow(infoWindowOptions);

            map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> {

                if (markerMap.get(marker)) {

                    infoWindow.close();
                    markerMap.put(marker, false);

                } else {

                    infoWindow.open(map, marker);
                    markerMap.put(marker, true);
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Method to get the carrier from mnc and modify map icon.
     *
     * @param mnc mobile network code
     * @param markerOptions options of the current marker
     * @return carrier name
     */
    private String getCarrier(int mnc, MarkerOptions markerOptions) {

        String carrier;

        switch (mnc) {

            case 1:
                carrier = "Telekom";
                markerOptions.icon("https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=T|DF137A|FFFFFF");
                //markerOptions.icon("http://maps.google.com/mapfiles/ms/icons/pink-dot.png");
                break;
            case 2:
                carrier = "Vodafone";
                markerOptions.icon("https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=V|E50B18|FFFFFF");
                //markerOptions.icon("http://maps.google.com/mapfiles/ms/icons/red-dot.png");
                break;
            case 3:
            case 7:
                carrier = "O2/e-plus";
                markerOptions.icon("https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=O|10237D|FFFFFF");
                ////markerOptions.icon("http://maps.google.com/mapfiles/ms/icons/blue-dot.png");
                break;
            default:
                carrier = "Unbekannter Provider";
                markerOptions.icon("https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=U|646464|FFFFFF");
                ////markerOptions.icon("http://maps.google.com/mapfiles/ms/icons/orange-dot.png");
        }

        return carrier;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Method as Fallback to start JavaFX Application.
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) throws IOException {

        loadSharedObject();
        launch(args);
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Method to load the jnetpcap library for the used operation system.
     *
     * @throws IOException if library not found
     */
    private static void loadSharedObject() throws IOException {

        // ToDo: fix for other platforms than linux amd64

        InputStream is = ClassLoader.class.getResourceAsStream("/native/linux/amd64/libjnetpcap.so");
        File file = File.createTempFile("lib", ".so");
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }

        is.close();
        os.close();

        System.load(file.getAbsolutePath());
        file.deleteOnExit();
    }
}
