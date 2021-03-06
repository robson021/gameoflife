package robert.model;

import robert.gui.CellPane;

import java.awt.*;
import java.util.List;

/**
 * Created by robert on 25.04.16.
 */
public class StaticStructure extends AbstractStructure {

    /*private static final Color[] colors = new Color[]{Color.YELLOW, Color.ORANGE,
            Color.CYAN, Color.PINK.darker(), Color.WHITE, Color.MAGENTA};*/
    public static final int SIZE = 3;

    public StaticStructure(List<CellPane> cells) {
        super(cells, StructureType.STATIC);
    }

    @Override
    public void move() {
        //System.out.println("Static structure can not move.");
        for (CellPane c : this.myCells) {
            //c.changeColor(colors[random.nextInt(colors.length)]);
            c.changeColor(Color.MAGENTA.darker().darker());
        }
    }
}
