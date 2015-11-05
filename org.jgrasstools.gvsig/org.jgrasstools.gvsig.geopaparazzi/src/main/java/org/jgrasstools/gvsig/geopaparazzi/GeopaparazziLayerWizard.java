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
import org.gvsig.fmap.mapcontext.rendering.legend.styling.ILabelingStrategy;
import org.gvsig.fmap.mapcontext.rendering.symbols.SymbolManager;
import org.gvsig.symbology.SymbologyLocator;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.impl.SingleSymbolLegend;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.persistence.PersistenceManager;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;

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
        try {
            MapContextManager mapContextManager = MapContextLocator.getMapContextManager();
            LinkedHashMap<String, SimpleFeatureCollection> layers = controller.getLayerName2FCMap();
            for( Entry<String, SimpleFeatureCollection> entry : layers.entrySet() ) {
                FeatureStore featureStore = GtGvsigConversionUtilities.toGvsigMemoryFeatureStore(entry.getValue());

                String name = entry.getKey();
                name = name.replaceFirst(GeopaparazziPanelController.FORM_NOTES_PREFIX, "");
                FLyrVect layer = (FLyrVect) mapContextManager.createLayer(name, featureStore);

                this.getMapContext().getLayers().addLayer(layer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
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
