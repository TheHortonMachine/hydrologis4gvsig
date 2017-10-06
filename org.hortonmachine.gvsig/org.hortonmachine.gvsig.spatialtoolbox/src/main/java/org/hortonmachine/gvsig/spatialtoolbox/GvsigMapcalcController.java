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

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;

import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.swing.api.Component;
import org.hortonmachine.gears.utils.files.FileUtilities;
import org.hortonmachine.gui.utils.GuiBridgeHandler;
import org.hortonmachine.gvsig.base.LayerUtilities;
import org.hortonmachine.gvsig.base.ProjectUtilities;
import org.hortonmachine.mapcalc.MapcalcController;
import org.jaitools.JAITools;
import org.jaitools.jiffle.Jiffle;
import org.jaitools.jiffle.runtime.JiffleRuntime;

import com.sun.media.jai.imageioimpl.ImageReadWriteSpi;

/**
 * The mapcalc view controller.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GvsigMapcalcController extends MapcalcController implements Component {
    private static final long serialVersionUID = 1L;

    private MapControl mapControl;
    private HashMap<String, FLyrRaster> rasterLayerMap = new HashMap<String, FLyrRaster>();

    public GvsigMapcalcController( GuiBridgeHandler guiBridge ) {
        super(guiBridge, false);
    }
    
    @Override
    protected void preInit() {
        mapControl = ProjectUtilities.getCurrentMapcontrol();
        super.preInit();
    }

    protected File getFileForLayerName( String layerName ) {
        String path = getFromLayers(layerName);
        if (path != null) {
            return new File(path);
        }
        return null;
    }

    /**
     * If a list of file backed layers is available, override this and do the conversion.
     * 
     * @param comboItem the name of the layer.
     * @return if available the path of the file backed layer.
     */
    protected String getFromLayers( String comboItem ) {
        try {
            FLyrRaster fLyrRaster = rasterLayerMap.get(comboItem);
            if (fLyrRaster != null) {
                File file = LayerUtilities.getFileFromRasterFileLayer(fLyrRaster);
                if (file != null && file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // protected void addMouseListenerToContext( MouseListener listener ) {
    // if (mapControl != null) {
    // mapControl.addMouseListener(listener);
    // }
    // }
    //
    // protected void removeMouseListenerFromContext( MouseListener listener ) {
    // if (mapControl != null) {
    // mapControl.removeMouseListener(listener);
    // }
    // }

    public void isVisibleTriggered() {

        rasterLayerMap.clear();
        MapContext currentMapcontext = ProjectUtilities.getCurrentMapcontext();
        List<FLyrRaster> rasterLayers = LayerUtilities.getRasterLayers(currentMapcontext);
        String[] rasterNames = new String[rasterLayers.size()];
        for( int i = 0; i < rasterNames.length; i++ ) {
            FLyrRaster fLyrRaster = rasterLayers.get(i);
            rasterNames[i] = fLyrRaster.getName();
            rasterLayerMap.put(rasterNames[i], fLyrRaster);
        }

        String[] layerNames = rasterLayerMap.keySet().toArray(new String[0]);
        setLayerNames(layerNames);
    }

    protected void loadRasterLayer( File file ) {
        try {
            String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
            LayerUtilities.loadRasterFile2Layer(file, nameWithoutExtention);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
