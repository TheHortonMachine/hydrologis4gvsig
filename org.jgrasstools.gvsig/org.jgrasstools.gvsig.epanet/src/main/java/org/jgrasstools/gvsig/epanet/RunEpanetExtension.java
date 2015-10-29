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
import org.jgrasstools.gvsig.epanet.core.RunEpanetWizard;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.OmsEpanetFeaturesSynchronizer;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.EpanetFeatureTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Andami extension to sync epanet shapefiles for further processing.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RunEpanetExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(RunEpanetExtension.class);

    private static final String ACTION_RUNEPANET = "run-epanet";

    private I18nManager i18nManager;

    private ApplicationManager applicationManager;

    private ProjectManager projectManager;

    private ThreadSafeDialogsManager dialogManager;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "run_epanet", this);

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
        if (ACTION_RUNEPANET.equalsIgnoreCase(actionCommand)) {
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

                SimpleFeatureCollection jFC = SyncEpanetShapefilesExtension.toFc(layers,
                        EpanetFeatureTypes.Junctions.ID.getName());
                SimpleFeatureCollection piFC = SyncEpanetShapefilesExtension.toFc(layers, EpanetFeatureTypes.Pipes.ID.getName());
                if (jFC == null || piFC == null) {
                    dialogManager.messageDialog(
                            "Could not find any pipes and junctions layer in the current view. Check your data.", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                RunEpanetWizard wizard = new RunEpanetWizard();
                wizard.setVisible(true);
            } catch (Exception e) {
                logger.error("ERROR", e);
                dialogManager.messageDialog("An error occurred while starting the Run Epanet wizard.", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
            }

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
