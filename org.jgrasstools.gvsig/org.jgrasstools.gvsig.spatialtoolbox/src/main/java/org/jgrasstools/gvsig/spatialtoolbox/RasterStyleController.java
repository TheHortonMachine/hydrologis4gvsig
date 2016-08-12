package org.jgrasstools.gvsig.spatialtoolbox;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.iterator.RandomIter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.geotools.coverage.grid.GridCoverage2D;
import org.gvsig.fmap.dal.coverage.RasterLocator;
import org.gvsig.fmap.dal.coverage.RasterManager;
import org.gvsig.fmap.dal.coverage.dataset.Buffer;
import org.gvsig.fmap.dal.coverage.datastruct.ColorItem;
import org.gvsig.fmap.dal.coverage.datastruct.DataStructFactory;
import org.gvsig.fmap.dal.coverage.datastruct.NoData;
import org.gvsig.fmap.dal.coverage.datastruct.Params;
import org.gvsig.fmap.dal.coverage.exception.ProcessInterruptedException;
import org.gvsig.fmap.dal.coverage.exception.QueryException;
import org.gvsig.fmap.dal.coverage.grid.RasterFilter;
import org.gvsig.fmap.dal.coverage.grid.RasterFilterList;
import org.gvsig.fmap.dal.coverage.grid.RasterFilterListManager;
import org.gvsig.fmap.dal.coverage.store.RasterDataStore;
import org.gvsig.fmap.dal.coverage.store.RasterQuery;
import org.gvsig.fmap.dal.coverage.store.props.ColorTable;
import org.gvsig.fmap.dal.coverage.store.props.Statistics;
import org.gvsig.fmap.dal.coverage.store.props.Transparency;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.raster.impl.datastruct.DefaultNoData;
import org.gvsig.tools.dynobject.DynObject;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.gvsig.base.DefaultGvsigTables;
import org.jgrasstools.gvsig.base.JGrasstoolsExtension;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.RasterStyleWrapper;
import org.jgrasstools.gvsig.base.RasterUtilities;
import org.jgrasstools.gvsig.base.StyleUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RasterStyleController extends RasterStyleView implements Component {
    private static final Logger logger = LoggerFactory.getLogger(RasterStyleController.class);
    private static final String DEFAULT_NUMFORMAT = "0.00";
    private static final String CUSTOM_RASTER_STYLES_KEY = "CUSTOM_RASTER_STYLES";

    private HashMap<String, FLyrRaster> rasterLayerMap;
    private MapContext currentMapcontext;
    private DynObject preferences;
    private HashMap<String, String> prefsMap = new HashMap<>();
    private ThreadSafeDialogsManager dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

    public RasterStyleController() {
        setPreferredSize(new Dimension(600, 300));

        preferences = ProjectUtilities.getPluginPreferences(JGrasstoolsExtension.class);
        Object prefsMapTmp = preferences.getDynValue(CUSTOM_RASTER_STYLES_KEY);
        if (prefsMapTmp != null) {
            prefsMap = (HashMap) prefsMapTmp;
        }
        new DefaultGvsigTables();

        init();
    }

    private void init() {

        Object selectedRaster = null;
        List<FLayer> selectedLayers = LayerUtilities.getSelectedLayers(null);
        if (selectedLayers.size() > 0) {
            FLayer selectedLayer = selectedLayers.get(0);
            if (selectedLayer instanceof FLyrRaster) {
                selectedRaster = selectedLayer.getName();
            }
        }

        setCombos(selectedRaster);
        numFormatField.setText(DEFAULT_NUMFORMAT);
        interpolatedCheckbox.setSelected(true);

        addComponentListener(new ComponentListener(){

            public void componentShown( ComponentEvent e ) {
            }

            public void componentResized( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentHidden( ComponentEvent e ) {
                freeResources();
            }
        });

        colortablesCombo.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                String colorTableText = customStyleArea.getText();
                if (colorTableText.trim().length() == 0 && colortablesCombo != null) {
                    Object selectedItem = colortablesCombo.getSelectedItem();
                    if (selectedItem != null) {
                        String colorTableName = selectedItem.toString();
                        logger.debug("Selected colorTableName: " + colorTableName);
                        if (colorTableName.trim().length() > 0) {
                            String tableString = new DefaultGvsigTables().getTableString(colorTableName);
                            customStyleArea.setText(tableString);
                        }
                    }
                }
            }
        });

        applyTableButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                applyStyle();
            }
        });

        customStyleButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                String name = dialogManager.inputDialog("Enter a name for the colortable", "Colotable name");
                if (name == null || name.trim().length() == 0) {
                    return;
                }
                String colorTableText = customStyleArea.getText();

                String tableString = new DefaultGvsigTables().getTableString(name);
                if (tableString != null) {
                    int answer = dialogManager.confirmDialog("A colortable with that name already exists. Overwrite it?",
                            "WARNING", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                DefaultGvsigTables.addRuntimeTable(name, colorTableText);

                String[] tableNames = DefaultGvsigTables.getTableNames();
                String[] tableNames2 = new String[tableNames.length + 1];
                System.arraycopy(tableNames, 0, tableNames2, 1, tableNames.length);
                tableNames2[0] = "";
                colortablesCombo.setModel(new DefaultComboBoxModel<String>(tableNames2));
                colortablesCombo.setSelectedItem(name);

                applyStyle();
            }
        });

    }

    protected void freeResources() {
        preferences.setDynValue(CUSTOM_RASTER_STYLES_KEY, prefsMap);
    }

    private void setCombos( Object selectedRaster ) {
        if (selectedRaster == null)
            selectedRaster = rasterLayerCombo.getSelectedItem();
        Object selectedColor = colortablesCombo.getSelectedItem();
        Object transparencyColor = opacityCombo.getSelectedItem();

        String[] rasterLayers = getRasterLayers();
        rasterLayerCombo.setModel(new DefaultComboBoxModel<String>(rasterLayers));

        String[] tableNames = DefaultGvsigTables.getTableNames();
        String[] tableNames2 = new String[tableNames.length + 1];
        System.arraycopy(tableNames, 0, tableNames2, 1, tableNames.length);
        tableNames2[0] = "";
        colortablesCombo.setModel(new DefaultComboBoxModel<String>(tableNames2));

        Integer[] transparency = new Integer[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        opacityCombo.setModel(new DefaultComboBoxModel<Integer>(transparency));
        opacityCombo.setSelectedItem(100);

        if (selectedRaster != null) {
            rasterLayerCombo.setSelectedItem(selectedRaster);
            colortablesCombo.setSelectedItem(selectedColor);
            opacityCombo.setSelectedItem(transparencyColor);
        }
    }

    public JComponent asJComponent() {
        return this;
    }

    public void isVisibleTriggered() {
        // MapContext newMapcontext = ProjectUtilities.getCurrentMapcontext();
        // if (newMapcontext == currentMapcontext) {
        // return;
        // }
        setCombos(null);
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

    private void applyStyle() {
        String layer = rasterLayerCombo.getSelectedItem().toString();
        FLyrRaster fLyrRaster = rasterLayerMap.get(layer);
        String colorTableName = colortablesCombo.getSelectedItem().toString();

        String novalueText = novalueTextfield.getText();
        Double novalue = null;
        if (novalueText.trim().length() > 0) {
            try {
                novalue = Double.parseDouble(novalueText);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        RasterDataStore dataStore = fLyrRaster.getDataStore();

        NoData noData = dataStore.getNoDataValue();

        try {
            Statistics stats = RasterUtilities.getRasterStatistics(dataStore, novalue, true);
            min = stats.getMinimun();
            max = stats.getMaximun();
        } catch (Exception e1) {
            logger.error("Error while reading raster data for colortable...", e1);
        }

        int opacity = 100;
        if (opacityCombo.getSelectedItem() != null)
            opacity = (Integer) opacityCombo.getSelectedItem();
        opacity = (int) (opacity * 255 / 100.0);

        logger.info("min=" + min + " max=" + max + " transp=" + opacity);

        String numFormatPattern = numFormatField.getText();
        if (numFormatPattern.trim().length() == 0) {
            numFormatPattern = DEFAULT_NUMFORMAT;
        }
        try {
            fLyrRaster.setLastLegend(null);

            RasterStyleWrapper rasterStyleWrapper = StyleUtilities.createRasterLegend4Colortable(colorTableName, min, max,
                    opacity, numFormatPattern, true);
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

}
