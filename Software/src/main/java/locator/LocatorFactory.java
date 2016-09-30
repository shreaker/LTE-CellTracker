package locator;

import locator.google.GoogleLocator;
import locator.opencellid.OpenCellIDLocator;

public class LocatorFactory {

    public Locator getLocator(String locatorType){

        Locator locator;

        switch (locatorType) {

            case "Google":
                locator = GoogleLocator.getInstance();
                break;
            case "OpenCellID":
                locator = OpenCellIDLocator.getInstance();
                break;
            default:
                locator = null;
        }

        return locator;
    }
}
