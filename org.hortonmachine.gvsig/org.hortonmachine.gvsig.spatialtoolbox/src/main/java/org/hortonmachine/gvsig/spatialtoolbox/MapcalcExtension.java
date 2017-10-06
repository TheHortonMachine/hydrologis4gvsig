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
package org.hortonmachine.gvsig.spatialtoolbox;

import org.gvsig.andami.plugins.Extension;
import org.gvsig.app.ApplicationManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.hortonmachine.gvsig.base.GvsigBridgeHandler;

/**
 * Andami extension to generate tiles from a view.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class MapcalcExtension extends Extension {

    private static final String ACTION_MAPCALC = "run-mapcalc";

    private ApplicationManager applicationManager;

    private GvsigMapcalcController mapcalcController;

    public void initialize() {
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
        if (ACTION_MAPCALC.equalsIgnoreCase(actionCommand)) {
            mapcalcController = new GvsigMapcalcController(new GvsigBridgeHandler());
            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            windowManager.showWindow(mapcalcController.asJComponent(), "The HortonMachine Raster Map Calculator", MODE.WINDOW);
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
        if (mapcalcController != null)
            mapcalcController.isVisibleTriggered();
        return true;
    }

}
