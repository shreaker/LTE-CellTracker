package locator.google;

import java.io.Serializable;
import java.util.List;

public class GeoLocRequestPOJO implements Serializable {

    private final boolean considerIp = false;
    private String radioType;
    private List<CellTowerPOJO> cellTowers;

    ////////////////////////////////////////////////////////////////////////////

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }

    ////////////////////////////////////////////////////////////////////////////

    public void setCellTowers(List<CellTowerPOJO> cellTowers) {
        this.cellTowers = cellTowers;
    }

    ////////////////////////////////////////////////////////////////////////////

    public class CellTowerPOJO {

        private int cellId;
        private int locationAreaCode;
        private int mobileCountryCode;
        private int mobileNetworkCode;

        public void setMobileNetworkCode(int mobileNetworkCode) {
            this.mobileNetworkCode = mobileNetworkCode;
        }

        public void setMobileCountryCode(int mobileCountryCode) {
            this.mobileCountryCode = mobileCountryCode;
        }

        public void setLocationAreaCode(int locationAreaCode) {
            this.locationAreaCode = locationAreaCode;
        }

        public void setCellId(int cellId) {
            this.cellId = cellId;
        }
    }
}
