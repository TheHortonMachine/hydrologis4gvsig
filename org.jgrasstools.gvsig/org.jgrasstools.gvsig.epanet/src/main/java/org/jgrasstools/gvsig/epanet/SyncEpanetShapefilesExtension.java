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
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.exceptions.LoadLayerException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.io.vectorwriter.OmsVectorWriter;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
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
            try {
                /*
                 * TODO check if the active view is the right one
                 * and if the right layers are present.
                 */

                Document activeDocument = projectManager.getCurrentProject().getActiveDocument();
                ViewDocument view = null;
                if (activeDocument instanceof ViewDocument) {
                    view = (ViewDocument) activeDocument;
                    if (!view.getName().equals(i18nManager.getTranslation(CreateProjectFilesExtension.MY_VIEW_NAME))) {
                        dialogManager.messageDialog("Please select the Epanet Layer View to proceed.", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                FLayers layers = view.getMapContext().getLayers();

                SimpleFeatureCollection jFC = toFc(layers, EpanetFeatureTypes.Junctions.ID.getName());
                SimpleFeatureCollection piFC = toFc(layers, EpanetFeatureTypes.Pipes.ID.getName());
                if (jFC == null || piFC == null) {
                    dialogManager.messageDialog(
                            "Could not find any pipes and junctions layer in the current view. Check your data.", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                SimpleFeatureCollection tFC = toFc(layers, EpanetFeatureTypes.Tanks.ID.getName());
                SimpleFeatureCollection puFC = toFc(layers, EpanetFeatureTypes.Pumps.ID.getName());
                SimpleFeatureCollection vFC = toFc(layers, EpanetFeatureTypes.Valves.ID.getName());
                SimpleFeatureCollection rFC = toFc(layers, EpanetFeatureTypes.Reservoirs.ID.getName());

                final OmsEpanetFeaturesSynchronizer sync = new OmsEpanetFeaturesSynchronizer();
                IJGTProgressMonitor pm = new LogProgressMonitor();
                sync.pm = pm;

                sync.inJunctions = jFC;
                sync.inPipes = piFC;
                sync.inTanks = tFC;
                sync.inPumps = puFC;
                sync.inValves = vFC;
                sync.inReservoirs = rFC;

                // TODO add dtm support

                // if (dtmLayer != null) {
                // Display.getDefault().syncExec(new Runnable(){
                // public void run() {
                // Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                // boolean doElevation = MessageDialog.openQuestion(shell, "Elevation", "The layer "
                // +
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

                sync.process();
                if (sync.outWarning.length() != 0) {
                    dialogManager.messageDialog("Some of the features could not be synched:\n" + sync.outWarning, "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // get the base folder
                FLyrVect pipesLayer = (FLyrVect) layers.getLayer(EpanetFeatureTypes.Junctions.ID.getName());
                FeatureStore pipesStore = pipesLayer.getFeatureStore();
                DataStoreParameters parameters = pipesStore.getParameters();
                Object fileObj = parameters.getDynValue("shpfile");
                if (fileObj instanceof File) {
                    File shapefile = (File) fileObj;
                    File parentFile = shapefile.getParentFile();
                    File syncFolderFile = new File(parentFile, "synched");
                    if (!syncFolderFile.exists()) {
                        if (!syncFolderFile.mkdir()) {
                            dialogManager.messageDialog("Unable to create the output folder in: " + syncFolderFile, "ERROR",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // remove old layers
                    layers.removeLayer(EpanetFeatureTypes.Junctions.ID.getName());
                    layers.removeLayer(EpanetFeatureTypes.Pipes.ID.getName());
                    layers.removeLayer(EpanetFeatureTypes.Pumps.ID.getName());
                    layers.removeLayer(EpanetFeatureTypes.Valves.ID.getName());
                    layers.removeLayer(EpanetFeatureTypes.Tanks.ID.getName());
                    layers.removeLayer(EpanetFeatureTypes.Reservoirs.ID.getName());

                    // FIXME get epsg
                    String epsgCode = "EPSG:32632";

                    // write new files
                    SimpleFeatureCollection piFC2 = sync.inPipes;
                    if (piFC2 != null && piFC2.size() > 0) {
                        File outFile = new File(syncFolderFile, EpanetFeatureTypes.Pipes.ID.getShapefileName());
                        OmsVectorWriter.writeVector(outFile.getAbsolutePath(), piFC2);
                        addLayer(layers, outFile.getAbsolutePath(), epsgCode);
                    }
                    SimpleFeatureCollection jFC2 = sync.inJunctions;
                    if (jFC2 != null && jFC2.size() > 0) {
                        File outFile = new File(syncFolderFile, EpanetFeatureTypes.Junctions.ID.getShapefileName());
                        OmsVectorWriter.writeVector(outFile.getAbsolutePath(), jFC2);
                        addLayer(layers, outFile.getAbsolutePath(), epsgCode);
                    }
                    SimpleFeatureCollection puFC2 = sync.inPumps;
                    if (puFC2 != null && puFC2.size() > 0) {
                        File outFile = new File(syncFolderFile, EpanetFeatureTypes.Pumps.ID.getShapefileName());
                        OmsVectorWriter.writeVector(outFile.getAbsolutePath(), puFC2);
                        addLayer(layers, outFile.getAbsolutePath(), epsgCode);
                    }
                    SimpleFeatureCollection vFC2 = sync.inValves;
                    if (vFC2 != null && vFC2.size() > 0) {
                        File outFile = new File(syncFolderFile, EpanetFeatureTypes.Valves.ID.getShapefileName());
                        OmsVectorWriter.writeVector(outFile.getAbsolutePath(), vFC2);
                        addLayer(layers, outFile.getAbsolutePath(), epsgCode);
                    }
                    SimpleFeatureCollection tFC2 = sync.inTanks;
                    if (tFC2 != null && tFC2.size() > 0) {
                        File outFile = new File(syncFolderFile, EpanetFeatureTypes.Tanks.ID.getShapefileName());
                        OmsVectorWriter.writeVector(outFile.getAbsolutePath(), tFC2);
                        addLayer(layers, outFile.getAbsolutePath(), epsgCode);
                    }
                    SimpleFeatureCollection rFC2 = sync.inReservoirs;
                    if (rFC2 != null && rFC2.size() > 0) {
                        File outFile = new File(syncFolderFile, EpanetFeatureTypes.Reservoirs.ID.getShapefileName());
                        OmsVectorWriter.writeVector(outFile.getAbsolutePath(), rFC2);
                        addLayer(layers, outFile.getAbsolutePath(), epsgCode);
                    }

                    dialogManager.messageDialog("Synched shapefiles successfully created.", "INFO",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                logger.error("ERROR", e);
                dialogManager.messageDialog("An error occurred while synching the shapefiles.", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    public static SimpleFeatureCollection toFc( FLayers layers, String layerName ) throws Exception {
        FLayer layer = layers.getLayer(layerName);
        if (layer == null)
            return null;
        FLyrVect pipesLayer = (FLyrVect) layer;
        FeatureStore pipesStore = pipesLayer.getFeatureStore();
        return GtGvsigConversionUtilities.toGtFeatureCollection(pipesStore);
    }

    private void addLayer( FLayers layers, String path, String epsgCode ) throws LoadLayerException {
        File shapeFile = new File(path);
        String name = FileUtilities.getNameWithoutExtention(shapeFile);
        FeatureStore dataStore = JGTUtilities.openShape(shapeFile, epsgCode);
        FLyrVect layer = (FLyrVect) applicationManager.getMapContextManager().createLayer(name, dataStore);
        layer.setProperty("ViewerLayer", Boolean.TRUE);
        layers.addLayer(layer);
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
