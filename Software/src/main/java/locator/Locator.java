package locator;

import items.Cell;
import java.util.Set;

public interface Locator {

    /**
     * Update cells with coordinates.
     *
     * @param cells list of cells
     * @return number of coordinates found by locator
     */
    int insertCellCoords(Set<Cell> cells) throws Exception;
}
