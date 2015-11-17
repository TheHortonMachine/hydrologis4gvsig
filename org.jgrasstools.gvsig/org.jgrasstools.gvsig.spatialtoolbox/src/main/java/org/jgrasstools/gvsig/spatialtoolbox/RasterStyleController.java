package org.jgrasstools.gvsig.spatialtoolbox;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.iterator.RandomIter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.geotools.coverage.grid.GridCoverage2D;
import org.gvsig.fmap.dal.coverage.RasterLocator;
import org.gvsig.fmap.dal.coverage.datastruct.ColorItem;
import org.gvsig.fmap.dal.coverage.datastruct.NoData;
import org.gvsig.fmap.dal.coverage.datastruct.Params;
import org.gvsig.fmap.dal.coverage.grid.RasterFilter;
import org.gvsig.fmap.dal.coverage.grid.RasterFilterList;
import org.gvsig.fmap.dal.coverage.grid.RasterFilterListManager;
import org.gvsig.fmap.dal.coverage.store.RasterDataStore;
import org.gvsig.fmap.dal.coverage.store.RasterQuery;
import org.gvsig.fmap.dal.coverage.store.props.ColorTable;
import org.gvsig.fmap.dal.coverage.store.props.Transparency;
import org.gvsig.fmap.dal.raster.spi.CoverageStoreProvider;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.ILegend;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.raster.impl.datastruct.DefaultNoData;
import org.gvsig.raster.impl.store.DefaultRasterStore;
import org.gvsig.raster.impl.store.properties.DataStoreColorTable;
import org.gvsig.tools.swing.api.Component;
import org.jaitools.numeric.Statistic;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.modules.r.summary.OmsRasterSummary;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.colors.ColorTables;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.gvsig.base.DataUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.RasterStyleWrapper;
import org.jgrasstools.gvsig.base.StyleUtilities;
import org.jgrasstools.modules.RasterSummary;
import org.opengis.referencing.FactoryException;

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
        interpolatedCheckbox.setSelected(true);

        applyTableButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                String layer = rasterLayerCombo.getSelectedItem().toString();
                FLyrRaster fLyrRaster = rasterLayerMap.get(layer);
                String colorTableName = colortablesCombo.getSelectedItem().toString();

                RasterDataStore dataStore = fLyrRaster.getDataStore();
                // CoverageStoreProvider provider = dataStore.getProvider();

                NoData noData = null;
                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                ColorTable previousColorTable = dataStore.getColorTable();
                if (previousColorTable != null) {
                    List<ColorItem> colorItems = previousColorTable.getColorItems();
                    ColorItem first = colorItems.get(0);
                    ColorItem last = colorItems.get(colorItems.size());
                    min = first.getValue();
                    max = last.getValue();
                } else {
                    try {
                        File rasterFile = LayerUtilities.getFileFromRasterFileLayer(fLyrRaster);
                        noData = new DefaultNoData(-9999.0, -9999.0, rasterFile.getName());

                        GridCoverage2D coverage2D = OmsRasterReader.readRaster(rasterFile.getAbsolutePath());
                        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(coverage2D);
                        int cols = regionMap.getCols();
                        int rows = regionMap.getRows();
                        RandomIter elevIter = CoverageUtilities.getRandomIterator(coverage2D);
                        max = Double.NEGATIVE_INFINITY;
                        min = Double.POSITIVE_INFINITY;
                        for( int r = 0; r < rows; r++ ) {
                            for( int c = 0; c < cols; c++ ) {
                                double value = elevIter.getSampleDouble(c, r, 0);
                                if (isNovalue(value)) {
                                    continue;
                                }
                                max = Math.max(max, value);
                                min = Math.min(min, value);
                            }
                        }

                        // RasterSummary rasterSummary = new RasterSummary();
                        // rasterSummary.inRaster = rasterFile.getAbsolutePath();
                        // rasterSummary.doHistogram = false;
                        // rasterSummary.process();
                        // min = rasterSummary.outMin;
                        // max = rasterSummary.outMax;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                int transparency = (Integer) transparencyCombo.getSelectedItem();
                transparency = (int) (transparency * 255 / 100.0);

                String numFormatPattern = numFormatField.getText();
                if (numFormatPattern.trim().length() == 0) {
                    numFormatPattern = DEFAULT_NUMFORMAT;
                }
                try {
                    fLyrRaster.setLastLegend(null);

                    RasterStyleWrapper rasterStyleWrapper = StyleUtilities.createRasterLegend4Colortable(colorTableName, min, max,
                            transparency, numFormatPattern, true);
                    ColorTable colorTable = rasterStyleWrapper.colorTable;
                    // colorTable.compressPalette();

                    // fLyrRaster.setLastLegend(colorTable);

                    // if (dataStore instanceof DefaultRasterStore) {
                    // DefaultRasterStore defaultRasterStore = (DefaultRasterStore) dataStore;
                    // defaultRasterStore.setColorTable(colorTable);
                    // }

                    Class< ? extends FLyrRaster> class1 = fLyrRaster.getClass();
                    Field field;
                    try {
                        field = class1.getDeclaredField("lastLegend");
                    } catch (Exception e1) {
                        Class superClass = class1.getSuperclass();
                        field = superClass.getDeclaredField("lastLegend");
                    }
                    field.setAccessible(true);
                    field.set(fLyrRaster, rasterStyleWrapper.legend);

                    RasterFilterList filterList = fLyrRaster.getRender().getFilterList();
                    RasterFilterListManager cManager = filterList.getManagerByID("ColorTable");
                    filterList.remove("colortable");
                    fLyrRaster.setLastLegend(null);
                    filterList.removeAll();
                    Transparency renderingTransparency = fLyrRaster.getRender().getRenderingTransparency();
                    if (noData != null)
                        renderingTransparency.setNoData(noData);
                    filterList.addEnvParam("Transparency", renderingTransparency);
                    Params params = filterList.createEmptyFilterParams();
                    params.setParam("colorTable", colorTable);
                    cManager.addFilter(params);
                    fLyrRaster.setLastLegend(colorTable);
                    for( int i = 0; i < filterList.lenght(); i++ ) {
                        ((RasterFilter) filterList.get(i)).setEnv(filterList.getEnv());
                    }
                    fLyrRaster.getRender().setFilterList(filterList);

                    // fLyrRaster.reload();
                    fLyrRaster.getMapContext().invalidate();

                    // MapControl currentMapcontrol = ProjectUtilities.getCurrentMapcontrol();
                    // if (currentMapcontrol != null) {
                    // currentMapcontrol.repaint();
                    // }
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
        // MapContext newMapcontext = ProjectUtilities.getCurrentMapcontext();
        // if (newMapcontext == currentMapcontext) {
        // return;
        // }
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
