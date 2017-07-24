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

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JComponent;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.documents.view.toc.AbstractTocContextMenuAction;
import org.gvsig.app.project.documents.view.toc.ITocItem;
import org.gvsig.fmap.dal.coverage.dataset.Buffer;
import org.gvsig.fmap.dal.coverage.datastruct.Extent;
import org.gvsig.fmap.dal.coverage.store.RasterDataStore;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.geom.primitive.Point;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.extensionpoint.ExtensionPoint;
import org.jgrasstools.gui.utils.GuiUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.RasterUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RasterValuesCopyExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(RasterValuesCopyExtension.class);

    private static final String ACTION_RASTERVALUESCOPY = "copy-raster-values";

    private static final String RASTERVALUESCOPY_TITLE = "Raster Values Copy";
    private static final String RASTERVALUESCOPY_DESCRIPTION = "Copies the current visible values of the selected raster";

    public void initialize() {
        IconThemeHelper.registerIcon("action", "raster_icon", this);
    }

    public void postInitialize() {
        ExtensionPoint exPoint = ToolsLocator.getExtensionPointManager().add(JGTUtilities.VIEW_TOCACTIONS_KEY, "");
        exPoint.append(RASTERVALUESCOPY_TITLE, RASTERVALUESCOPY_DESCRIPTION, new RasterStyleAction(RASTERVALUESCOPY_TITLE));
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
            return 2;
        }

        @Override
        public String getText() {
            return name;
        }

        @Override
        public void execute( ITocItem item, FLayer[] selectedItems ) {
            RasterValuesCopyExtension.this.execute(ACTION_RASTERVALUESCOPY);
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
            return isVisible;
        }

    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_RASTERVALUESCOPY.equalsIgnoreCase(actionCommand)) {
            FLyrRaster selectedRaster = null;
            List<FLayer> selectedLayers = LayerUtilities.getSelectedLayers(null);
            if (selectedLayers.size() > 0) {
                FLayer selectedLayer = selectedLayers.get(0);
                if (selectedLayer instanceof FLyrRaster) {
                    selectedRaster = (FLyrRaster) selectedLayer;
                }
            }
            if (selectedRaster != null) {
                MapContext mapcontext = ProjectUtilities.getCurrentMapcontext();
                if (mapcontext != null) {
                    Envelope envelope = mapcontext.getViewPort().getEnvelope();
                    Point ll = envelope.getLowerCorner();
                    Point ur = envelope.getUpperCorner();

                    double w = ll.getX();
                    double s = ll.getY();
                    double e = ur.getX();
                    double n = ur.getY();

                    // Extent ext = new ExtentImpl(w, n, e, s);
                    try {

                        RasterDataStore dataStore = selectedRaster.getDataStore();
                        Extent extent = dataStore.getExtent();

                        Point2D llP = new Point2D.Double(w, s);
                        Point2D urP = new Point2D.Double(e, n);

                        Point2D llPix = dataStore.worldToRaster(llP);
                        Point2D urPix = dataStore.worldToRaster(urP);

                        int fromC = (int) Math.floor(llPix.getX());
                        int toC = (int) Math.ceil(urPix.getX());
                        int fromR = (int) Math.floor(urPix.getY());
                        int toR = (int) Math.ceil(llPix.getY());

                        int cellCount = (toC - fromC) * (toR - fromR);
                        if (cellCount > 1E5) {
                            ApplicationManager applicationManager = ApplicationLocator.getManager();
                            JComponent mainComponent = applicationManager.getActiveDocument().getMainComponent();
                            GuiUtilities.showWarningMessage(mainComponent, "WARNING",
                                    "This command supports only up to 100000 cells. The current view contains: " + cellCount);
                            return;
                        }

                        DecimalFormat f = new DecimalFormat("0.0000000");

                        Buffer buffer = RasterUtilities.readSingleBandRasterData(dataStore, extent, null);
                        int dataType = buffer.getDataType();
                        StringBuilder sb = new StringBuilder();
                        sb.append("{\n");
                        for( int r = fromR; r < toR; r++ ) {
                            if (r > fromR) {
                                sb.append(",\n ");
                            }
                            sb.append("{ ");
                            for( int c = fromC; c < toC; c++ ) {
                                if (c > fromC) {
                                    sb.append(",");
                                }
                                if (dataType == Buffer.TYPE_BYTE) {
                                    byte value = buffer.getElemByte(r, c, 0);
                                    sb.append(value);
                                } else if ((dataType == Buffer.TYPE_SHORT) | (dataType == Buffer.TYPE_USHORT)) {
                                    short value = buffer.getElemShort(r, c, 0);
                                    sb.append(value);
                                } else if (dataType == Buffer.TYPE_INT) {
                                    int value = buffer.getElemInt(r, c, 0);
                                    sb.append(value);
                                } else if (dataType == Buffer.TYPE_FLOAT) {
                                    float value = buffer.getElemFloat(r, c, 0);
                                    sb.append(f.format(value));
                                } else if (dataType == Buffer.TYPE_DOUBLE) {
                                    double value = buffer.getElemDouble(r, c, 0);
                                    sb.append(f.format(value));
                                } else {
                                    sb.append(" -nv- ");
                                }
                            }
                            sb.append(" }");
                        }
                        sb.append("\n}");

                        GuiUtilities.copyToClipboard(sb.toString());
                        ApplicationManager applicationManager = ApplicationLocator.getManager();
                        JComponent mainComponent = applicationManager.getActiveDocument().getMainComponent();
                        GuiUtilities.showInfoMessage(mainComponent, "INFO",
                                "Current selected raster values copied to clipboard. Cells: " + cellCount);
                    } catch (Exception e1) {
                        logger.error("Error", e1);
                    }

                }
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
