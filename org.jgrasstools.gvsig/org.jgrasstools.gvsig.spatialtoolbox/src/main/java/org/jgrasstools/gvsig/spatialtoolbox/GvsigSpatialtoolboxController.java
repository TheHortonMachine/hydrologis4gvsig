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

import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.exceptions.LoadLayerException;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.swing.api.Component;
import org.jgrasstools.gears.io.vectorreader.OmsVectorReader;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gui.spatialtoolbox.SpatialtoolboxController;
import org.jgrasstools.gui.utils.GuiBridgeHandler;
import org.jgrasstools.gvsig.base.DataUtilities;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;

/**
 * The spatialtoolbox view controller.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GvsigSpatialtoolboxController extends SpatialtoolboxController implements Component {
    private static final long serialVersionUID = 1L;

    private MapControl mapControl;
    private HashMap<String, FLyrVect> vectorLayerMap;
    private HashMap<String, FLyrRaster> rasterLayerMap;

    public GvsigSpatialtoolboxController( GuiBridgeHandler guiBridge ) {
        super(guiBridge);
    }

    @Override
    protected void preInit() {
        mapControl = ProjectUtilities.getCurrentMapcontrol();
        super.preInit();
    }

    /**
     * If a list of file backed layers is available, override this and do the conversion.
     * 
     * @param comboItem the name of the layer.
     * @return if available the path of the file backed layer.
     */
    protected String getFromLayers( String comboItem ) {
        try {
            FLyrVect fLyrVect = vectorLayerMap.get(comboItem);
            if (fLyrVect != null) {
                File file = LayerUtilities.getFileFromVectorFileLayer(fLyrVect);
                if (file != null && file.exists()) {
                    return file.getAbsolutePath();
                }
            } else {
                FLyrRaster fLyrRaster = rasterLayerMap.get(comboItem);
                if (fLyrRaster != null) {
                    File file = LayerUtilities.getFileFromRasterFileLayer(fLyrRaster);
                    if (file != null && file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void addMouseListenerToContext( MouseListener listener ) {
        if (mapControl != null) {
            mapControl.addMouseListener(listener);
        }
    }

    protected void removeMouseListenerFromContext( MouseListener listener ) {
        if (mapControl != null) {
            mapControl.removeMouseListener(listener);
        }
    }

    public void isVisibleTriggered() {
        vectorLayerMap = new HashMap<String, FLyrVect>();
        rasterLayerMap = new HashMap<String, FLyrRaster>();

        MapContext currentMapcontext = ProjectUtilities.getCurrentMapcontext();
        List<FLyrVect> vectorLayers = LayerUtilities.getVectorLayers(currentMapcontext);
        String[] vectorNames = new String[vectorLayers.size()];
        for( int i = 0; i < vectorNames.length; i++ ) {
            FLyrVect fLyrVect = vectorLayers.get(i);
            vectorNames[i] = fLyrVect.getName();
            vectorLayerMap.put(vectorNames[i], fLyrVect);
        }
        List<FLyrRaster> rasterLayers = LayerUtilities.getRasterLayers(currentMapcontext);
        String[] rasterNames = new String[rasterLayers.size()];
        for( int i = 0; i < rasterNames.length; i++ ) {
            FLyrRaster fLyrRaster = rasterLayers.get(i);
            rasterNames[i] = fLyrRaster.getName();
            rasterLayerMap.put(rasterNames[i], fLyrRaster);
        }
        pPanel.setVectorRasterLayers(vectorNames, rasterNames);

        if (mapControl == null) {
            mapControl = ProjectUtilities.getCurrentMapcontrol();
            if (mapControl != null) {
                mapControl.addMouseListener(pPanel);
            }
        } else {
            mapControl.removeMouseListener(pPanel);
            mapControl = ProjectUtilities.getCurrentMapcontrol();
            if (mapControl != null) {
                mapControl.addMouseListener(pPanel);
            }
        }

    }

    protected void loadRasterLayer( File file ) {
        try {
            String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
            LayerUtilities.loadRasterFile2Layer(file, nameWithoutExtention);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadVectorLayer( File file ) {
        try {
            ReferencedEnvelope readEnvelope = OmsVectorReader.readEnvelope(file.getAbsolutePath());
            String epsgCode = GtGvsigConversionUtilities.gtCrs2Epsg(readEnvelope.getCoordinateReferenceSystem());
            FeatureStore featureStore = DataUtilities.getShapefileDatastore(file, epsgCode);
            String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
            LayerUtilities.loadFeatureStore2Layer(featureStore, nameWithoutExtention);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
