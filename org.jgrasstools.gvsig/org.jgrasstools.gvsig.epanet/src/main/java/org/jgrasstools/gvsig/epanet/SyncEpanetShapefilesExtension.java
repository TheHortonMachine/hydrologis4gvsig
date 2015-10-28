/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.gvsig.epanet;

import java.io.File;

import javax.swing.JOptionPane;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.Document;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.app.project.documents.view.ViewManager;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.ProviderNotRegisteredException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureQuery;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.exceptions.LoadLayerException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.landregistryviewer.LandRegistryViewerParcel;
import org.gvsig.landregistryviewer.impl.DefaultLandRegistryViewerParcel;
import org.gvsig.landregistryviewer.impl.IntersectsEvaluator;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.OmsEpanetFeaturesSynchronizer;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.EpanetFeatureTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Andami extension to sync epanet shapefiles for further processing.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SyncEpanetShapefilesExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(SyncEpanetShapefilesExtension.class);

    private static final String ACTION_SYNCSHPS = "sync-epanet-shpfiles";

    private I18nManager i18nManager;

    private ApplicationManager applicationManager;

    private ProjectManager projectManager;

    private ThreadSafeDialogsManager dialogManager;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "sync_shps", this);

        i18nManager = ToolsLocator.getI18nManager();
        applicationManager = ApplicationLocator.getManager();

        projectManager = applicationManager.getProjectManager();
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
    }

    public void postInitialize() {
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_SYNCSHPS.equalsIgnoreCase(actionCommand)) {
            // Set the tool in the mapcontrol of the active view.

            IWindow activeWindow = applicationManager.getActiveWindow();
            if (activeWindow == null) {
                return;
            }

            /*
             * TODO check if the active view is the right one
             * and if the right layers are present.
             */

            // ViewManager viewManager = (ViewManager)
            // projectManager.getDocumentManager(ViewManager.TYPENAME);

            Document activeDocument = projectManager.getCurrentProject().getActiveDocument();
            ViewDocument view = null;
            if (activeDocument instanceof ViewDocument) {
                view = (ViewDocument) activeDocument;
                if (!view.getName().equals(i18nManager.getTranslation(CreateProjectFilesExtension.MY_VIEW_NAME))) {
                    dialogManager.messageDialog("Please select the Epanet Layer View to proceed.", "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            FLayers layers = view.getMapContext().getLayers();

            FLyrVect pipesLayer = (FLyrVect) layers.getLayer(EpanetFeatureTypes.Pipes.ID.getName());
            FeatureStore pipesStore = pipesLayer.getFeatureStore();
            

            
            

            SimpleFeatureCollection jFC = null;
            SimpleFeatureCollection piFC = null;
            SimpleFeatureCollection tFC = null;
            SimpleFeatureCollection puFC = null;
            SimpleFeatureCollection vFC = null;
            SimpleFeatureCollection rFC = null;

            final OmsEpanetFeaturesSynchronizer sync = new OmsEpanetFeaturesSynchronizer();
            IJGTProgressMonitor pm = new LogProgressMonitor();
            sync.pm = pm;

            sync.inJunctions = jFC;
            sync.inPipes = piFC;
            if (tFC != null)
                sync.inTanks = tFC;
            if (puFC != null)
                sync.inPumps = puFC;
            if (vFC != null)
                sync.inValves = vFC;
            if (rFC != null)
                sync.inReservoirs = rFC;
            // if (dtmLayer != null) {
            // Display.getDefault().syncExec(new Runnable(){
            // public void run() {
            // Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            // boolean doElevation = MessageDialog.openQuestion(shell, "Elevation", "The layer " +
            // dtmLayer.getName()
            // + " will be used to extract elevation data were needed. Is this ok?");
            // if (doElevation) {
            // try {
            // GridCoverage2D coverage = (GridCoverage2D) dtmLayer.getResource(Coverage.class,
            // new NullProgressMonitor());
            // sync.inElev = coverage;
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            // }
            // }
            // });
            // }
            try {
                sync.process();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // if (sync.outWarning.length() != 0) {
            // Display.getDefault().syncExec(new Runnable(){
            // public void run() {
            // Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            // MessageDialog.openInformation(shell, "Missing properties",
            // "Some of the features could not be synched:\n" + sync.outWarning);
            // }
            // });
            // return;
            // }
            //
            // SimpleFeatureCollection jFC2 = sync.inJunctions;
            // SimpleFeatureCollection piFC2 = sync.inPipes;
            // SimpleFeatureCollection puFC2 = sync.inPumps;
            // SimpleFeatureCollection vFC2 = sync.inValves;
            // SimpleFeatureCollection tFC2 = sync.inTanks;
            // SimpleFeatureCollection rFC2 = sync.inReservoirs;

        }
    }

    private void addLayer( String path, ViewDocument view, String epsgCode ) throws LoadLayerException {
        File shapeFile = new File(path);
        String name = FileUtilities.getNameWithoutExtention(shapeFile);
        FeatureStore dataStore = openShape(shapeFile, epsgCode);
        FLyrVect layer = (FLyrVect) applicationManager.getMapContextManager().createLayer(name, dataStore);
        // Add a new property to the layer to identify it.
        layer.setProperty("ViewerLayer", Boolean.TRUE);
        // Add this layer to the mapcontext of the new view.
        view.getMapContext().getLayers().addLayer(layer);
    }

    /**
     * Open the file as a feature store of type shape.
     *
     * @param shape file to be opened
     * @param epsgCode 
     *
     * @return the feature store
     */
    private FeatureStore openShape( File shape, String epsgCode ) {
        try {
            DataManager manager = DALLocator.getDataManager();
            DataStoreParameters parameters = manager.createStoreParameters("Shape");
            parameters.setDynValue("shpfile", shape);
            parameters.setDynValue("crs", "EPSG:" + epsgCode);
            return (FeatureStore) manager.openStore("Shape", parameters);
        } catch (InitializeException e) {
            logger.error(e.getMessageStack());
            throw new RuntimeException(e);
        } catch (ProviderNotRegisteredException e) {
            logger.error(e.getMessageStack());
            throw new RuntimeException(e);
        } catch (ValidateDataParametersException e) {
            logger.error(e.getMessageStack());
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if tools of this extension are enabled.
     */
    public boolean isEnabled() {
        //
        // By default the tool is always enabled
        //
        return true;
    }

    /**
     * Check if tools of this extension are visible.
     */
    public boolean isVisible() {
        return true;
    }

}
