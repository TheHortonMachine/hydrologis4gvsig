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
package org.hortonmachine.gvsig.utils;

import java.io.File;
import java.util.List;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.app.project.documents.view.toc.AbstractTocContextMenuAction;
import org.gvsig.app.project.documents.view.toc.ITocItem;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.extensionpoint.ExtensionPoint;
import org.hortonmachine.gui.utils.GuiUtilities;
import org.hortonmachine.gvsig.base.DataUtilities;
import org.hortonmachine.gvsig.base.HMExtension;
import org.hortonmachine.gvsig.base.HMUtilities;
import org.hortonmachine.gvsig.base.LayerUtilities;
import org.opengis.referencing.FactoryException;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class OpenFilemanagerOnPathExtension extends HMExtension {

    private static final String ACTION_OPENFILEONPATH = "open-file-on-path";

    private static final String OPENFILEONPATH_TITLE = "Open file path";
    private static final String OPENFILEONPATH_DESCRIPTION = "Opens the system file browser in the current file path";

    public void initialize() {
        IconThemeHelper.registerIcon("action", "folder", this);
    }

    public void postInitialize() {
        ExtensionPoint exPoint = ToolsLocator.getExtensionPointManager().add(HMUtilities.VIEW_TOCACTIONS_KEY, "");
        exPoint.append(OPENFILEONPATH_TITLE, OPENFILEONPATH_DESCRIPTION, new OpenOnFileAction(OPENFILEONPATH_TITLE));
    }

    private final class OpenOnFileAction extends AbstractTocContextMenuAction {
        private String name;
        public OpenOnFileAction( String name ) {
            this.name = name;
        }

        @Override
        public String getGroup() {
            return DataUtilities.FILE_TOOLS_GROUP;
        }

        @Override
        public int getGroupOrder() {
            return 2;
        }

        @Override
        public String getText() {
            return name;
        }

        @Override
        public void execute( ITocItem item, FLayer[] selectedItems ) {
            OpenFilemanagerOnPathExtension.this.execute(ACTION_OPENFILEONPATH);
        }

        @Override
        public boolean isEnabled( ITocItem item, FLayer[] selectedItems ) {
            return true;
        }

        @Override
        public boolean isVisible( ITocItem item, FLayer[] selectedItems ) {
            if (selectedItems.length == 1) {
                FLayer selLayer = selectedItems[0];
                if (selLayer instanceof FLyrRaster) {
                    try {
                        LayerUtilities.getFileFromRasterFileLayer((FLyrRaster) selLayer);
                        return true;
                    } catch (FactoryException e) {
                        return false;
                    }
                } else if (selLayer instanceof FLyrVect) {
                    try {
                        LayerUtilities.getFileFromVectorFileLayer((FLyrVect) selLayer);
                        return true;
                    } catch (FactoryException e) {
                        return false;
                    }
                }
            }
            return false;
        }

    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_OPENFILEONPATH.equalsIgnoreCase(actionCommand)) {
            List<FLayer> selectedLayers = LayerUtilities.getSelectedLayers(null);
            try {
                if (selectedLayers.size() == 1) {
                    FLayer selLayer = selectedLayers.get(0);
                    if (selLayer instanceof FLyrRaster) {
                        File file = LayerUtilities.getFileFromRasterFileLayer((FLyrRaster) selLayer);
                        GuiUtilities.openFile(file.getParentFile());
                    } else if (selLayer instanceof FLyrVect) {
                        File file = LayerUtilities.getFileFromVectorFileLayer((FLyrVect) selLayer);
                        GuiUtilities.openFile(file.getParentFile());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        return true;
    }

}
