package org.jgrasstools.gvsig.spatialtoolbox;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.gvsig.fmap.dal.coverage.RasterLocator;
import org.gvsig.fmap.dal.coverage.store.RasterDataStore;
import org.gvsig.fmap.dal.coverage.store.RasterQuery;
import org.gvsig.fmap.dal.raster.spi.CoverageStoreProvider;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.ILegend;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.swing.api.Component;
import org.jgrasstools.gears.utils.colors.ColorTables;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.StyleUtilities;

public class RasterStyleController extends RasterStyleView implements Component {

    private static final String DEFAULT_NUMFORMAT = "#.00";
    private HashMap<String, FLyrRaster> rasterLayerMap;
    private MapContext currentMapcontext;

    public RasterStyleController() {
        setPreferredSize(new Dimension(600, 300));
        init();
    }

    private void init() {
        setCombos();
        numFormatField.setText(DEFAULT_NUMFORMAT);

        applyTableButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                String layer = rasterLayerCombo.getSelectedItem().toString();
                FLyrRaster fLyrRaster = rasterLayerMap.get(layer);
                String colorTableName = colortablesCombo.getSelectedItem().toString();

                RasterDataStore dataStore = fLyrRaster.getDataStore();
                CoverageStoreProvider provider = dataStore.getProvider();

                double min = 1045.4;
                double max = 2393.9;
                int transparency = (Integer) transparencyCombo.getSelectedItem();
                transparency = (int) (transparency * 255 / 100.0);

                String numFormatPattern = numFormatField.getText();
                if (numFormatPattern.trim().length() == 0) {
                    numFormatPattern = DEFAULT_NUMFORMAT;
                }
                try {
                    fLyrRaster.setLastLegend(null);
                    ILegend legend = StyleUtilities.createRasterLegend4Colortable(colorTableName, min, max, transparency,
                            numFormatPattern);

                    Class< ? extends FLyrRaster> class1 = fLyrRaster.getClass();
                    Field field;
                    try {
                        field = class1.getDeclaredField("lastLegend");
                    } catch (Exception e1) {
                        Class superClass = class1.getSuperclass();
                        field = superClass.getDeclaredField("lastLegend");
                    }
                    field.setAccessible(true);
                    field.set(fLyrRaster, legend);
//                    fLyrRaster.reload();

                    fLyrRaster.getMapContext().invalidate();
//                    MapControl currentMapcontrol = ProjectUtilities.getCurrentMapcontrol();
//                    if (currentMapcontrol != null) {
//                        currentMapcontrol.repaint();
//                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    private void setCombos() {
        String[] rasterLayers = getRasterLayers();
        rasterLayerCombo.setModel(new DefaultComboBoxModel<String>(rasterLayers));
        ColorTables[] values = ColorTables.values();
        String[] colorTables = new String[values.length];
        for( int i = 0; i < colorTables.length; i++ ) {
            colorTables[i] = values[i].name();
        }
        colortablesCombo.setModel(new DefaultComboBoxModel<String>(colorTables));

        Integer[] transparency = new Integer[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        transparencyCombo.setModel(new DefaultComboBoxModel<Integer>(transparency));
        transparencyCombo.setSelectedItem(100);
    }

    public JComponent asJComponent() {
        return this;
    }

    public void isVisibleTriggered() {
        setCombos();
    }

    private String[] getRasterLayers() {
        rasterLayerMap = new HashMap<String, FLyrRaster>();

        currentMapcontext = ProjectUtilities.getCurrentMapcontext();
        List<FLyrRaster> rasterLayers = LayerUtilities.getRasterLayers(currentMapcontext);
        String[] rasterNames = new String[rasterLayers.size()];
        for( int i = 0; i < rasterNames.length; i++ ) {
            FLyrRaster fLyrRaster = rasterLayers.get(i);
            rasterNames[i] = fLyrRaster.getName();
            rasterLayerMap.put(rasterNames[i], fLyrRaster);
        }
        return rasterNames;
    }

}
