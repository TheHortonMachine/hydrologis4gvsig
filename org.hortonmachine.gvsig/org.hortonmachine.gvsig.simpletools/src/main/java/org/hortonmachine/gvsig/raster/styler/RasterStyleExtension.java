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
package org.hortonmachine.gvsig.raster.styler;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.app.project.documents.view.toc.AbstractTocContextMenuAction;
import org.gvsig.app.project.documents.view.toc.ITocItem;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.extensionpoint.ExtensionPoint;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.hortonmachine.gvsig.base.HMUtilities;
import org.hortonmachine.gvsig.base.RasterUtilities;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RasterStyleExtension extends Extension {

    private static final String SINGLE_BAND_RASTER_STYLER_TITLE = "Single Band Raster Styler";
    private static final String SINGLE_BAND_RASTER_STYLER_DESCRIPTION = "A simple Single Band Raster Styler";

    private static final String ACTION_RASTERSTYLE = "style-singleband-raster";

    private RasterStyleController rasterStyleController;

    // private ActionInfo action;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "raster_icon", this);
    }

    public void postInitialize() {
        ExtensionPoint exPoint = ToolsLocator.getExtensionPointManager().add(HMUtilities.VIEW_TOCACTIONS_KEY, "");
        exPoint.append(SINGLE_BAND_RASTER_STYLER_TITLE, SINGLE_BAND_RASTER_STYLER_DESCRIPTION,
                new RasterStyleAction(SINGLE_BAND_RASTER_STYLER_TITLE));
    }

    private final class RasterStyleAction extends AbstractTocContextMenuAction {
        private String name;
        public RasterStyleAction( String name ) {
            this.name = name;
        }

        @Override
        public String getGroup() {
            return RasterUtilities.RASTER_TOOLS_GROUP;
        }

        @Override
        public int getGroupOrder() {
            return 1;
        }

        @Override
        public String getText() {
            return name;
        }

        @Override
        public void execute( ITocItem item, FLayer[] selectedItems ) {
            RasterStyleExtension.this.execute(ACTION_RASTERSTYLE);
        }

        @Override
        public boolean isEnabled( ITocItem item, FLayer[] selectedItems ) {
            return true;
        }

        @Override
        public boolean isVisible( ITocItem item, FLayer[] selectedItems ) {
            boolean isVisible = false;
            if (selectedItems.length > 0) {
                for( FLayer selectedLayer : selectedItems ) {
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
                    if (isVisible) {
                        break;
                    }
                }
            }
            if (rasterStyleController != null)
                rasterStyleController.isVisibleTriggered();
            return isVisible;
        }

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
