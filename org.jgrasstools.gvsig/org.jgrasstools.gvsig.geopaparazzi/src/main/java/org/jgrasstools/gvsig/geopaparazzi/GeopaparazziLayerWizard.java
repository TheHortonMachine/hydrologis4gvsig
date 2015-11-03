package org.jgrasstools.gvsig.geopaparazzi;

import java.awt.BorderLayout;

import org.gvsig.app.gui.WizardPanel;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStore;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.ProviderNotRegisteredException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.exceptions.LoadLayerException;
import org.gvsig.fmap.mapcontext.layers.FLayer;

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
        DataStoreParameters[] parameters = controller.getParameters();

        // TODO add layer for each parameter

        DataManager dataManager = DALLocator.getDataManager();
        MapContextManager mapContextManager = MapContextLocator.getMapContextManager();
        try {
            DataStore store = dataManager.openStore("sqlite", parameters[0]);
            FLayer layer = mapContextManager.createLayer(store.getName(), store);

            this.getMapContext().getLayers().addLayer(layer);
        } catch (ValidateDataParametersException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InitializeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProviderNotRegisteredException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LoadLayerException e) {
            // TODO Auto-generated catch block
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
