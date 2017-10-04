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
package org.hortonmachine.gvsig.raster.graphics;

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
public class RasterGraphicsExtension extends Extension {

    private static final String ACTION_RASTERGRAPHIC = "quick-raster-graphics";

    private static final String RASTERGRAPHICS_TITLE = "Raster Graphics View";
    private static final String RASTERGRAPHICS_DESCRIPTION = "Shows as mapoverlay the values of the raster map.";

    private RasterGraphicsController rasterGraphicsController;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "raster_icon", this);
    }

    public void postInitialize() {
        ExtensionPoint exPoint = ToolsLocator.getExtensionPointManager().add(HMUtilities.VIEW_TOCACTIONS_KEY, "");
        exPoint.append(RASTERGRAPHICS_TITLE, RASTERGRAPHICS_DESCRIPTION,
                new RasterGraphicsAction(RASTERGRAPHICS_TITLE));
    }

    private final class RasterGraphicsAction extends AbstractTocContextMenuAction {
        private String name;
        public RasterGraphicsAction( String name ) {
            this.name = name;
        }

        @Override
        public String getGroup() {
            return RasterUtilities.RASTER_TOOLS_GROUP;
        }

        @Override
        public int getGroupOrder() {
            return 3;
        }

        @Override
        public String getText() {
            return name;
        }

        @Override
        public void execute( ITocItem item, FLayer[] selectedItems ) {
            RasterGraphicsExtension.this.execute(ACTION_RASTERGRAPHIC);
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
            if (rasterGraphicsController != null)
                rasterGraphicsController.isVisibleTriggered();
            return isVisible;
        }

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
        if (rasterGraphicsController != null)
            rasterGraphicsController.isVisibleTriggered();
        return true;
    }

}
