package org.jgrasstools.gvsig.geopaparazzi;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.gvsig.app.gui.WizardPanel;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.ILegend;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorLegend;
import org.gvsig.fmap.mapcontext.rendering.legend.styling.ILabelingStrategy;
import org.gvsig.fmap.mapcontext.rendering.symbols.SymbolManager;
import org.gvsig.symbology.SymbologyLocator;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.impl.SingleSymbolLegend;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.persistence.PersistenceManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;
import org.jgrasstools.gvsig.base.utils.console.LogConsoleController;

public class GeopaparazziLayerWizard extends WizardPanel {

    private GeopaparazziPanelController controller;

    @Override
    public void initWizard() {
        controller = new GeopaparazziPanelController();

        this.setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();

        constr.gridwidth = GridBagConstraints.RELATIVE;
        constr.gridheight = GridBagConstraints.RELATIVE;
        constr.fill = GridBagConstraints.BOTH;
        constr.anchor = GridBagConstraints.FIRST_LINE_START;
        constr.weightx = 1;
        constr.weighty = 1;
        add(controller, constr);

        setTabName("Geopaparazzi");
    }

    @Override
    public void execute() {
        WindowManager windowManager = ToolsSwingLocator.getWindowManager();
        final IJGTProgressMonitor pm = new LogProgressMonitor();
        final LogConsoleController logConsole = new LogConsoleController(pm);
        windowManager.showWindow(logConsole.asJComponent(), "Geopaparazzi data extraction", MODE.WINDOW);

        new Thread(new Runnable(){
            public void run() {
                try {
                    logConsole.beginProcess("GeopaparazziDataStore");
                    loadLayers(pm);
                    logConsole.finishProcess();
                    logConsole.stopLogging();
                    logConsole.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadLayers( IJGTProgressMonitor pm ) {
        try {
            MapContextManager mapContextManager = MapContextLocator.getMapContextManager();
            LinkedHashMap<String, SimpleFeatureCollection> layerData = controller.getLayerName2FCMap();
            LinkedHashMap<String, IVectorLegend> legends = controller.getLayerName2LegendMap();
            LinkedHashMap<String, ILabelingStrategy> labelings = controller.getLayerName2LabelingMap();
            pm.beginTask("Load layers...", layerData.size());
            for( Entry<String, SimpleFeatureCollection> entry : layerData.entrySet() ) {
                FeatureStore featureStore = GtGvsigConversionUtilities.toGvsigMemoryFeatureStore(entry.getValue());

                String name = entry.getKey();
                name = name.replaceFirst(GeopaparazziPanelController.FORM_NOTES_PREFIX, "");
                FLyrVect layer = (FLyrVect) mapContextManager.createLayer(name, featureStore);
                IVectorLegend legend = legends.get(name);
                if (legend != null) {
                    layer.setLegend(legend);
                }

                ILabelingStrategy labelingStrategy = labelings.get(name);
                if (labelingStrategy != null) {
                    layer.setLabelingStrategy(labelingStrategy);
                    layer.setIsLabeled(true);
                }

                this.getMapContext().getLayers().addLayer(layer);
                pm.worked(1);
            }
            pm.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public DataStoreParameters[] getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

}
