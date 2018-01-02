/*
 * This file is part of HortonMachine (http://www.hortonmachine.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * The HortonMachine is free software: you can redistribute it and/or modify
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
package org.hortonmachine.gvsig.geopaparazzi;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.app.ApplicationManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.hortonmachine.gvsig.base.GvsigBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class ProjectsViewerExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(ProjectsViewerExtension.class);

    private static final String ACTION_GEOPAP_VIEWER = "open-geopaparazzi-viewer";

    private ApplicationManager applicationManager;

    private GvsigGeopaparazziViewer geopaparazziViewer;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "database_picture", this);
        // i18nManager = ToolsLocator.getI18nManager();
        // applicationManager = ApplicationLocator.getManager();
        //
        // projectManager = applicationManager.getProjectManager();
        // dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
    }

    public void postInitialize() {
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_GEOPAP_VIEWER.equalsIgnoreCase(actionCommand)) {
            geopaparazziViewer = new GvsigGeopaparazziViewer(new GvsigBridgeHandler());
            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            windowManager.showWindow(geopaparazziViewer.asJComponent(), "GvSIG Mobile/Geopaparazzi Projects Viewer", MODE.WINDOW);
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
        if (geopaparazziViewer != null)
            geopaparazziViewer.isVisibleTriggered();
        return true;
    }

}
