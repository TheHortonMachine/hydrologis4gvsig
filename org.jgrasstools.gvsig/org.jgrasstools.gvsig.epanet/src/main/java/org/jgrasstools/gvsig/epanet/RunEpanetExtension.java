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

import javax.swing.JOptionPane;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.epanet.core.RunEpanetWizard;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.EpanetFeatureTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Andami extension to run epanet.
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
                MapContext currentMapcontext = ProjectUtilities.getCurrentMapcontext();
                if (currentMapcontext==null) {
                    dialogManager.messageDialog("Please select a map view to proceed.", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                FLayers layers = currentMapcontext.getLayers();

                FLayer jLayer = layers.getLayer(EpanetFeatureTypes.Junctions.ID.getName());
                FLayer pLayer = layers.getLayer(EpanetFeatureTypes.Pipes.ID.getName());
                if (jLayer == null || pLayer == null) {
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
