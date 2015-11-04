package org.jgrasstools.gvsig.geopaparazzi;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

public class GeopaparazziMetadata extends AbstractTableModel {

    private static final long serialVersionUID = -2678580144252993562L;

    private String[] columnNames = {"Name", "Value"};
    private Object[][] data;

    public GeopaparazziMetadata( LinkedHashMap<String, String> metadataMap ) {
        this.data = new Object[metadataMap.size()][getColumnCount()];

        int i = 0;
        for( Entry<String, String> entry : metadataMap.entrySet() ) {
            data[i][0] = entry.getKey();
            data[i][1] = entry.getValue();
            i++;
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName( int col ) {
        return columnNames[col];
    }

    public Object getValueAt( int i, int j ) {
        return data[i][j];
    }

}
