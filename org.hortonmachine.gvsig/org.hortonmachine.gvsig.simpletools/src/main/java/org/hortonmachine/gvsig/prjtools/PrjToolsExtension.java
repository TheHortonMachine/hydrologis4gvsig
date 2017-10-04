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
package org.hortonmachine.gvsig.prjtools;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PrjToolsExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(PrjToolsExtension.class);

    private static final String ACTION_SETPRJ = "open-prj-tools";

    private PrjToolsController prjToolsController;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "copy", this);
    }

    public void postInitialize() {
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_SETPRJ.equalsIgnoreCase(actionCommand)) {
            prjToolsController = new PrjToolsController();
            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            windowManager.showWindow(prjToolsController.asJComponent(), "Prj Tools", MODE.WINDOW);
        }
    }

    /**
     * Check if tools of this extension are enabled.
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Check if tools of this extension are visible.
     */
    public boolean isVisible() {
        if (prjToolsController != null)
            prjToolsController.isVisibleTriggered();
        return true;
    }

}
