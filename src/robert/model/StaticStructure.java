package robert.model;

import robert.gui.CellPane;

import java.util.List;

/**
 * Created by robert on 25.04.16.
 */
public class StaticStructure extends AbstractStructure {

    public StaticStructure(List<CellPane> cells, StructureType type) {
        super(cells, type);
    }

    @Override
    public void move() {
        System.out.println("Static structure can not move.");
    }
}