package parser;

import items.Cell;
import java.util.Set;

public interface Parser {

    /**
     * Get cells from parser.
     *
     * @param fileName location of file to parse
     */
    void getCells(String fileName, Set<Cell> cells);
}
