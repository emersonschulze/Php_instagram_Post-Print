/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

public class MyDefaultTableModel extends DefaultTableModel {
    
    List<Color> rowColours = Arrays.asList(
        Color.RED,
        Color.GREEN,
        Color.CYAN
    );

    public MyDefaultTableModel(Object[][] data, String[] colunas2) {
       super(data, colunas2);
    }

    public void setRowColour(int row, Color c) {
        rowColours.set(row, c);
        fireTableRowsUpdated(row, row);
    }

    public Color getRowColour(int row) {
        return rowColours.get(row);
    }

    public boolean isCellEditable(int row, int column){
        return false;
    }

    public Class getColumnClass(int column){
        if(column == 0){
            return ImageIcon.class;
        }
        return getValueAt(0, column).getClass();
    }
    
}
