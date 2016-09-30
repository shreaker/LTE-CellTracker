package utilities;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;

public class Config {

    private String apiKeyGoogle = null;
    private String apiKeyOpenCellId = null;

    /**
     * Read configuration ini file.
     *
     * @param filePath
     * @throws ConfigException
     */
    public void readConfigIni(String filePath) throws ConfigException {
        try {
            Ini ini = new Ini(new File(filePath));
            apiKeyGoogle = ini.get("API-KEY", "google");
            apiKeyOpenCellId = ini.get("API-KEY", "openCellId");
        } catch (IOException e) {
            throw new ConfigException();
        }
    }

    public String getApiKeyGoogle() {
        return apiKeyGoogle;
    }

    public String getApiKeyOpenCellId() {
        return apiKeyOpenCellId;
    }
}
