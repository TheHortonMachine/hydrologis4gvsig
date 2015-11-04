package org.jgrasstools.gvsig.geopaparazzi;

import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.gvsig.app.gui.WizardPanel;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;

public class GeopaparazziLayerWizard extends WizardPanel {

    private GeopaparazziPanelController controller;

    @Override
    public void initWizard() {
        setLayout(new BorderLayout());
        controller = new GeopaparazziPanelController();

        add(controller, BorderLayout.NORTH);
        setTabName("Geopaparazzi");
    }

    @Override
    public void execute() {
        try {
            MapContextManager mapContextManager = MapContextLocator.getMapContextManager();
            LinkedHashMap<String, SimpleFeatureCollection> layers = controller.getLayerName2FCMap();
            for( Entry<String, SimpleFeatureCollection> entry : layers.entrySet() ) {
                FeatureStore featureStore = GtGvsigConversionUtilities.toGvsigMemoryFeatureStore(entry.getKey(),
                        entry.getValue());

                FLayer layer = mapContextManager.createLayer(entry.getKey(), featureStore);
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
