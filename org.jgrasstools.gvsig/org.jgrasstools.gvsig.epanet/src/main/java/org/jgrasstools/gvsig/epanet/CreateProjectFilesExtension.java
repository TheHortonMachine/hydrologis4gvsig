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

import java.awt.GridBagConstraints;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JOptionPane;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.app.project.documents.view.ViewManager;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.crs.CRSFactory;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.ProviderNotRegisteredException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.exceptions.LoadLayerException;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.OmsEpanetProjectFilesGenerator;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.EpanetFeatureTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Andami extension to create Epanet project files.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class CreateProjectFilesExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(CreateProjectFilesExtension.class);

    public static final String MY_VIEW_NAME = "Epanet Layer View";

    private static final String ACTION_CREATEPROJECTFILES = "create-epanet-project-files";

    private I18nManager i18nManager;

    private ApplicationManager applicationManager;

    private ProjectManager projectManager;

    private ThreadSafeDialogsManager dialogManager;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "new_project", this);

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
        if (ACTION_CREATEPROJECTFILES.equalsIgnoreCase(actionCommand)) {
            // Set the tool in the mapcontrol of the active view.

            File lastPath = JGTUtilities.getLastFile();
            File[] showOpenDirectoryDialog = dialogManager
                    .showOpenDirectoryDialog("Choose a folder into which to create the project files", lastPath);
            if (showOpenDirectoryDialog.length == 0) {
                return;
            }
            File folder = showOpenDirectoryDialog[0];
            JGTUtilities.setLastPath(folder.getAbsolutePath());

            String epsgCode = dialogManager.inputDialog("Please insert the CRS EPSG code for the required projection.",
                    "EPSG code");
            if (!epsgCode.toUpperCase().startsWith("EPSG")) {
                epsgCode = "EPSG:" + epsgCode;
            }
            generateProjectShapefiles(folder, epsgCode);

        }
    }

    public void generateProjectShapefiles( final File baseFolder, String epsgCode ) {
        LogProgressMonitor pm = new LogProgressMonitor();
        try {
            /*
             * TODO define the styles
             */

            // Create a new view and set the name.
            ViewManager viewManager = (ViewManager) projectManager.getDocumentManager(ViewManager.TYPENAME);
            ViewDocument view = (ViewDocument) viewManager.createDocument();
            view.setName(i18nManager.getTranslation(MY_VIEW_NAME));
            view.getMapContext().setProjection(CRSFactory.getCRS(epsgCode));
            
            OmsEpanetProjectFilesGenerator gen = new OmsEpanetProjectFilesGenerator();
            gen.pm = pm;
            gen.pCode = epsgCode;
            gen.inFolder = baseFolder.getAbsolutePath();
            gen.process();
            // Create a new layer for each created shapefile
            String piPath = baseFolder.getAbsolutePath() + File.separator + EpanetFeatureTypes.Pipes.ID.getShapefileName();
            addLayer(piPath, view, epsgCode);
            String jPath = baseFolder.getAbsolutePath() + File.separator + EpanetFeatureTypes.Junctions.ID.getShapefileName();
            addLayer(jPath, view, epsgCode);
            String tPath = baseFolder.getAbsolutePath() + File.separator + EpanetFeatureTypes.Tanks.ID.getShapefileName();
            addLayer(tPath, view, epsgCode);
            String puPath = baseFolder.getAbsolutePath() + File.separator + EpanetFeatureTypes.Pumps.ID.getShapefileName();
            addLayer(puPath, view, epsgCode);
            String vPath = baseFolder.getAbsolutePath() + File.separator + EpanetFeatureTypes.Valves.ID.getShapefileName();
            addLayer(vPath, view, epsgCode);
            String rPath = baseFolder.getAbsolutePath() + File.separator + EpanetFeatureTypes.Reservoirs.ID.getShapefileName();
            addLayer(rPath, view, epsgCode);

            // 4. Add the view to the current project.
            projectManager.getCurrentProject().add(view);

            // 5. Force to show the view's window.
            IView viewWindow = (IView) viewManager.getMainWindow(view);
            applicationManager.getUIManager().addWindow(viewWindow, GridBagConstraints.CENTER);
            try {
                // 6. and maximise the window
                applicationManager.getUIManager().setMaximum((IWindow) viewWindow, true);
            } catch (PropertyVetoException e) {
                logger.info("Can't maximize view.", e);
            }

        } catch (Exception e) {
            String message = "An error occurred while creating the project files.";
            dialogManager.messageDialog(message, "ERROR", JOptionPane.ERROR_MESSAGE);

            logger.error(message, e);
        } finally {
            pm.done();
        }

    }

    private void addLayer( String path, ViewDocument view, String epsgCode ) throws LoadLayerException {
        File shapeFile = new File(path);
        String name = FileUtilities.getNameWithoutExtention(shapeFile);
        FeatureStore dataStore = JGTUtilities.openShape(shapeFile, epsgCode);
        FLyrVect layer = (FLyrVect) applicationManager.getMapContextManager().createLayer(name, dataStore);
        // Add a new property to the layer to identify it.
        layer.setProperty("ViewerLayer", Boolean.TRUE);
        // Add this layer to the mapcontext of the new view.
        view.getMapContext().getLayers().addLayer(layer);
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
