package locator.google;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import items.Cell;
import items.Coordinate;
import locator.Locator;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import utilities.Config;
import utilities.ConfigException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Client for Google Maps Geolocation API.
 */
public class GoogleLocator implements Locator {

    private static GoogleLocator instance;
    private static String LOCATOR_ID = "Google";

    ////////////////////////////////////////////////////////////////////////////

    private GeoLocRequestPOJO requestPOJO;
    private List<GeoLocRequestPOJO.CellTowerPOJO> cellTowerPOJOList;
    private GeoLocRequestPOJO.CellTowerPOJO cellTowerPOJO;
    private String requestUrl;

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Private constructor for singleton pattern.
     */
    private GoogleLocator() {
        try {
            initRestClient("https://www.googleapis.com/geolocation/v1/geolocate?key=", "./CellTracker.ini", RadioType.LTE);
        } catch (ConfigException ioExceptionConfigIni) {
            System.err.println("Error init " + LOCATOR_ID + " client");
            ioExceptionConfigIni.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * GoogleLocator singleton instance.
     *
     * @return GoogleLocator
     */
    public static synchronized GoogleLocator getInstance() {

        if (GoogleLocator.instance == null) {
            GoogleLocator.instance = new GoogleLocator();
        }

        return GoogleLocator.instance;
    }

    ////////////////////////////////////////////////////////////////////////////

    /** Initialize client. Must only be done once.
     * @param url
     * @param filePathIni
     * @param radioType
     */
    public void initRestClient(String url, String filePathIni, RadioType radioType) throws ConfigException {

        this.requestPOJO = new GeoLocRequestPOJO();
        this.requestPOJO.setRadioType(radioType.toString());
        this.cellTowerPOJOList = new LinkedList<>();
        this.cellTowerPOJO = requestPOJO.new CellTowerPOJO();
        this.requestUrl = url + readApiKey(filePathIni);
    }

    ////////////////////////////////////////////////////////////////////////////

    /** Read ApiKey from config file.
     * @param filePathIni Path and filename of config file.
     * @return apiKey
     */
    private String readApiKey(String filePathIni) throws ConfigException {

        Config config = new Config();
        config.readConfigIni(filePathIni);
        return config.getApiKeyGoogle();
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize POJO for API-REST request. Do this for every new request.
     * @param cellID
     * @param locationAreaCode
     * @param mobileCountryCode
     * @param mobileNetworkCode
     */
    private void initRequestForREST(int cellID, int locationAreaCode, int mobileCountryCode, int mobileNetworkCode) {

        cellTowerPOJO.setCellId(cellID);
        cellTowerPOJO.setLocationAreaCode(locationAreaCode);
        cellTowerPOJO.setMobileCountryCode(mobileCountryCode);
        cellTowerPOJO.setMobileNetworkCode(mobileNetworkCode);
        cellTowerPOJOList.add(cellTowerPOJO);
        requestPOJO.setCellTowers(cellTowerPOJOList);
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Execute API-REST POST-request with the data from the requestPOJO.
     * @return Fill repsonsePOJO with response-data.
     * @throws IOException see Google-Geolocation-API web page for error codes.
     */
    private GeoLocResponsePOJO getCoordinatesForMobileCell() throws GoogleLocatorException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(requestUrl);
            post.setHeader("content-type", "application/json");
            Gson gson = new GsonBuilder().create();
            String body = gson.toJson(requestPOJO);
            post.setEntity(new StringEntity(body));

            ResponseHandler<GeoLocResponsePOJO> responseHandler = response -> {

                StatusLine statusLine = response.getStatusLine();
                HttpEntity entity = response.getEntity();

                final int HTTP_ERROR_NR_START = 300;

                if (statusLine.getStatusCode() >= HTTP_ERROR_NR_START) {
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                }

                if (entity == null) {
                    throw new ClientProtocolException("Response contains no content");
                }

                Gson gson1 = new GsonBuilder().create();
                String responseString = EntityUtils.toString(entity);

                return gson1.fromJson(responseString, GeoLocResponsePOJO.class);
            };

            return httpClient.execute(post, responseHandler);

        } catch (IOException e) {
            throw new GoogleLocatorException();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    @Override
    public int insertCellCoords(Set<Cell> cells) throws Exception {

        int foundCells = 0;
        GeoLocResponsePOJO response = null;
        for (Cell cell : cells) {
            initRequestForREST(cell.getCid(), cell.getTac(), cell.getMcc(), cell.getMnc());
            try {
                response = getCoordinatesForMobileCell();
                cell.addCoordinates(LOCATOR_ID, new Coordinate(response.getLocation().getLatitude(), response.getLocation().getLongitude(), (int) response.getAccuracy()));
                foundCells++;
            } catch (GoogleLocatorException ioExceptionGeolocationGoogle) {
                ioExceptionGeolocationGoogle.printStackTrace();
            }
        }
        return foundCells;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get id of this Locator.
     * @return
     */
    public String getLocatorId() {
        return LOCATOR_ID;
    }
}
