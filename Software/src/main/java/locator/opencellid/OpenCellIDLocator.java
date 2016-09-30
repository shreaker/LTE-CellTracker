package locator.opencellid;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import items.Cell;
import items.Coordinate;
import locator.Locator;
import utilities.Config;
import utilities.ConfigException;

import java.io.IOException;
import java.util.Set;


public class OpenCellIDLocator implements Locator {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    ////////////////////////////////////////////////////////////////////////////
    private static OpenCellIDLocator instance;
    private static final String LOCATOR_ID = "OpenCellID";
    private static String apiKey;

    ////////////////////////////////////////////////////////////////////////////

    private OpenCellIDLocator() {
        try {
            apiKey = readApiKey("./CellTracker.ini");
        }catch  (ConfigException ioExceptionConfigIni) {
            System.err.println("Error init " + LOCATOR_ID + " client");
            ioExceptionConfigIni.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public static synchronized OpenCellIDLocator getInstance() {

        if (OpenCellIDLocator.instance == null) {
            OpenCellIDLocator.instance = new OpenCellIDLocator();
        }

        return OpenCellIDLocator.instance;
    }

    ////////////////////////////////////////////////////////////////////////////

    /** Read ApiKey from config file.
     * @param filePathIni Path and filename of config file.
     * @return apiKey
     */
    private String readApiKey(String filePathIni) throws ConfigException {

        Config config = new Config();
        config.readConfigIni(filePathIni);
        return config.getApiKeyOpenCellId();
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * ask opencellid api for cell id.
     *
     * @return
     * @throws IOException
     * @throws Exception
     */
    public int getCellPosFromOCI(Cell aCell) throws IOException {

        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) throws IOException {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        OpenCellIDUrl url = new OpenCellIDUrl("http://opencellid.org/cell/get").setCellInfo(aCell);

        HttpRequest request = requestFactory.buildGetRequest(url);
        CellFeed cellFeed = request.execute().parseAs(CellFeed.class);

        if (isResponseValid(aCell, cellFeed)) {
            aCell.addCoordinates(LOCATOR_ID, new Coordinate(cellFeed.lat, cellFeed.lon, cellFeed.range));
            return 1;
        }

        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     *  Checks if response from openCellId-server is valid.
     *  Response is invalid if no cell was found.
     * @param request
     * @param response
     * @return true if response is valid, else false.
     */
    private boolean isResponseValid(Cell request, CellFeed response) {
        if (response.cellid == null) {
            return false;
        } else if(request.getCid() != response.cellid){
            return false;
        }else {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    @Override
    public int insertCellCoords(Set<Cell> cells) throws IOException {

        int numberOfPos = 0;
        for (Cell cell : cells) {
            numberOfPos += getCellPosFromOCI(cell);
        }
        return numberOfPos;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Represents a cell location feed.
     *
     */
    public static class CellFeed {
        @Key("lon")
        public double lon;

        @Key("lat")
        public double lat;

        @Key("mcc")
        public Integer mcc;

        @Key("mnc")
        public Integer mnc;

        @Key("lac")
        public Integer lac;

        @Key("cellid")
        public Integer cellid;

        @Key("averageSignalStrength")
        public Integer averageSignalStrength;

        @Key("range")
        public Integer range;

        @Key("samples")
        public Integer samples;

        @Key("changeable")
        public Boolean changeable;

        @Key("radio")
        public String radio;

        @Key("sid")
        public Integer sid;

        @Key("nid")
        public Integer nid;

        @Key("bid")
        public Integer bid;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * URL for OpenCellID API.
     *
     * @author christian
     */
    public static class OpenCellIDUrl extends GenericUrl {

        @Key
        private final String key = apiKey;
        @Key
        public String radio = "LTE";
        @Key
        public String format = "json";
        @Key
        private Integer mcc;
        @Key
        private Integer mnc;
        @Key
        private Integer lac;
        @Key
        private Integer cellid;

        /**
         * Constructor.
         *
         * @param encodedUrl
         */
        public OpenCellIDUrl(String encodedUrl) {
            super(encodedUrl);
        }

        /**
         * set cell info for api request.
         *
         * @param cell with cellinfo of mcc, mnc, lac and cellid
         * @return
         */
        public OpenCellIDUrl setCellInfo(Cell cell) {

            this.mcc = cell.getMcc();
            this.mnc = cell.getMnc();
            this.lac = cell.getTac();
            this.cellid = cell.getCid();
            return this;
        }
    }
}
