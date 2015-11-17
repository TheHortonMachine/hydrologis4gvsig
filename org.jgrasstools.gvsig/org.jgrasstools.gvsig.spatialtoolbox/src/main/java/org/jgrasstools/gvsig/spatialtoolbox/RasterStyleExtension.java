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
package org.jgrasstools.gvsig.spatialtoolbox;

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
public class RasterStyleExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(RasterStyleExtension.class);

    private static final String ACTION_RASTERSTYLE = "style-singleband-raster";

    private RasterStyleController rasterStyleController;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "raster_icon", this);
    }

    public void postInitialize() {
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_RASTERSTYLE.equalsIgnoreCase(actionCommand)) {
            rasterStyleController = new RasterStyleController();
            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            windowManager.showWindow(rasterStyleController.asJComponent(), "Simple Single Band Raster Styler", MODE.WINDOW);
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
        if (rasterStyleController != null)
            rasterStyleController.isVisibleTriggered();
        return true;
    }

}
