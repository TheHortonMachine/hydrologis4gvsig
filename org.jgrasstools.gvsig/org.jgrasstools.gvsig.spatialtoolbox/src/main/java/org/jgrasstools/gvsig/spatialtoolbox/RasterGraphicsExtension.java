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

import java.util.List;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.PluginsLocator;
import org.gvsig.andami.actioninfo.ActionInfo;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.view.ViewManager;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.RasterUtilities;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RasterGraphicsExtension extends Extension {

    private static final String ACTION_RASTERGRAPHIC = "quick-raster-graphics";

    private RasterGraphicsController rasterGraphicsController;

    private ActionInfo action;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "raster_icon", this);
    }

    public void postInitialize() {

        ApplicationManager applicationManager = ApplicationLocator.getManager();

        ProjectManager projectManager = applicationManager.getProjectManager();
        ViewManager viewManager = (ViewManager) projectManager.getDocumentManager(ViewManager.TYPENAME);
        action = PluginsLocator.getActionInfoManager().getAction(ACTION_RASTERGRAPHIC);
        viewManager.addTOCContextAction(action, RasterUtilities.RASTER_TOOLS_GROUP, 3);
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_RASTERGRAPHIC.equalsIgnoreCase(actionCommand)) {
            rasterGraphicsController = new RasterGraphicsController();
            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            windowManager.showWindow(rasterGraphicsController.asJComponent(), "Raster Graphics View", MODE.WINDOW);
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
        List<FLayer> selectedLayers = LayerUtilities.getSelectedLayers(null);
        boolean isVisible = false;
        if (selectedLayers.size() > 0) {
            FLayer selectedLayer = selectedLayers.get(0);
            if (selectedLayer instanceof FLyrRaster) {
                int[] bandCountFromDataset = ((FLyrRaster) selectedLayer).getBandCountFromDataset();
                if (bandCountFromDataset.length > 1) {
                    isVisible = false;
                } else {
                    isVisible = true;
                }
            } else {
                isVisible = false;
            }
        }
        if (action != null)
            action.setActive(isVisible);

        if (rasterGraphicsController != null)
            rasterGraphicsController.isVisibleTriggered();
        return isVisible;
    }

}
