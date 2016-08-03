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
package org.jgrasstools.gvsig.base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cresques.cts.IProjection;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.fmap.dal.DataStore;
import org.gvsig.fmap.dal.coverage.RasterLocator;
import org.gvsig.fmap.dal.coverage.store.parameter.RasterDataParameters;
import org.gvsig.fmap.dal.coverage.util.ProviderServices;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemStoreParameters;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.exceptions.LoadLayerException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.raster.fmap.layers.DefaultFLyrRaster;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.dataTypes.DataType;
import org.opengis.referencing.FactoryException;

/**
 * Utils to handle layers.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class LayerUtilities {

    /**
     * Get the current selected layers from the {@link MapContext}.
     * 
     * @param mapContext the mapcontext.
     * @return the list of selected layers.
     */
    public static List<FLayer> getSelectedLayers( MapContext mapContext ) {
        List<FLayer> selectedLayers = new ArrayList<FLayer>();

        if (mapContext == null) {
            mapContext = ProjectUtilities.getCurrentMapcontext();
        }
        if (mapContext == null) {
            return selectedLayers;
        }
        FLayers layers = mapContext.getLayers();
        int layersCount = layers.getLayersCount();
        for( int i = 0; i < layersCount; i++ ) {
            FLayer layer = layers.getLayer(i);
            if (layer.isActive()) {
                selectedLayers.add(layer);
            }
        }
        return selectedLayers;
    }

    /**
     * Get all available vector layers from the {@link MapContext}.
     * 
     * @param mapContext the mapcontext.
     * @return the list of vector layers.
     */
    public static List<FLyrVect> getVectorLayers( MapContext mapContext ) {
        List<FLyrVect> vectorLayers = new ArrayList<FLyrVect>();

        if (mapContext == null) {
            mapContext = ProjectUtilities.getCurrentMapcontext();
        }
        if (mapContext == null) {
            return vectorLayers;
        }
        FLayers layers = mapContext.getLayers();
        int layersCount = layers.getLayersCount();
        for( int i = 0; i < layersCount; i++ ) {
            FLayer layer = layers.getLayer(i);
            if (layer instanceof FLyrVect) {
                FLyrVect vectorLayer = (FLyrVect) layer;
                vectorLayers.add(vectorLayer);
            }
        }
        return vectorLayers;
    }

    /**
     * Get all available raster layers from the {@link MapContext}.
     * 
     * @param mapContext the mapcontext.
     * @return the list of raster layers.
     */
    public static List<FLyrRaster> getRasterLayers( MapContext mapContext ) {
        List<FLyrRaster> rasterLayers = new ArrayList<FLyrRaster>();

        if (mapContext == null) {
            mapContext = ProjectUtilities.getCurrentMapcontext();
        }
        if (mapContext == null) {
            return rasterLayers;
        }
        FLayers layers = mapContext.getLayers();
        int layersCount = layers.getLayersCount();
        for( int i = 0; i < layersCount; i++ ) {
            FLayer layer = layers.getLayer(i);
            if (layer instanceof FLyrRaster) {
                FLyrRaster vectorLayer = (FLyrRaster) layer;
                rasterLayers.add(vectorLayer);
            }
        }
        return rasterLayers;
    }

    /**
     * Get the file on which a vector layer bases. 
     * 
     * <p>If the layer is not file based, it returns <code>null</code>.
     * 
     * @param vectorLayer the layer.
     * @return the file or <code>null</code>.
     * @throws FactoryException
     */
    public static File getFileFromVectorFileLayer( FLyrVect vectorLayer ) throws FactoryException {
        File file;
        try {
            FilesystemStoreParameters fsSParams = (FilesystemStoreParameters) vectorLayer.getDataStore().getParameters();
            file = fsSParams.getFile();
        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    /**
     * Get the file on which a raster layer bases. 
     * 
     * <p>If the layer is not file based, it returns <code>null</code>.
     * 
     * @param rasterLayer the layer.
     * @return the file or <code>null</code>.
     * @throws FactoryException
     */
    public static File getFileFromRasterFileLayer( FLyrRaster rasterLayer ) throws FactoryException {
        File file;
        try {
            RasterDataParameters rdParams = ((RasterDataParameters) rasterLayer.getDataStore().getParameters());
            file = new File(rdParams.getURI());
        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    /**
     * Get the projection from a vector layer. 
     * 
     * @param vectorLayer the layer.
     * @return the {@link IProjection}.
     * @throws FactoryException
     */
    public static IProjection getProjectionFromVectorFileLayer( FLyrVect vectorLayer ) throws FactoryException {
        IProjection crsObject = (IProjection) vectorLayer.getFeatureStore().getDynValue(DataStore.METADATA_CRS);
        return crsObject;
    }

    /**
     * Get the projection from a raster layer. 
     * 
     * @param rasterLayer the layer.
     * @return the {@link IProjection}.
     * @throws FactoryException
     */
    public static IProjection getIProjectionFromRasterFileLayer( FLyrRaster rasterLayer ) throws FactoryException {
        RasterDataParameters rdParams = ((RasterDataParameters) rasterLayer.getDataStore().getParameters());
        IProjection crsObject = (IProjection) rdParams.getSRS();
        return crsObject;
    }

    /**
     * Load a featurestore as a named layer.
     * 
     * @param featureStore the store to load.
     * @param layerName the name for the new layer.
     * @throws LoadLayerException
     */
    public static void loadFeatureStore2Layer( FeatureStore featureStore, String layerName ) throws LoadLayerException {
        MapContext mapContext = ProjectUtilities.getCurrentMapcontext();
        if (mapContext != null) {
            ApplicationManager applicationManager = ApplicationLocator.getManager();
            FLyrVect layer = (FLyrVect) applicationManager.getMapContextManager().createLayer(layerName, featureStore);
            layer.setProperty("ViewerLayer", Boolean.TRUE);
            mapContext.getLayers().addLayer(layer);
        }
    }

    /**
     * Load a raster file as a named layer.
     * 
     * @param rasterFile the file.
     * @param layerName the name for the new layer.
     * @throws LoadLayerException
     */
    public static void loadRasterFile2Layer( File rasterFile, String layerName ) throws LoadLayerException {
        MapContext mapContext = ProjectUtilities.getCurrentMapcontext();
        if (mapContext != null) {
            ProviderServices provServ = RasterLocator.getManager().getProviderServices();
            RasterDataParameters storeParameters = provServ.createParameters(rasterFile.getName());
            storeParameters.setURI(rasterFile.toURI());

            MapContextManager mcm = MapContextLocator.getMapContextManager();
            DefaultFLyrRaster rasterLayer = (DefaultFLyrRaster) mcm.createLayer(layerName, storeParameters);

            mapContext.getLayers().addLayer(rasterLayer);
        }
    }

    /**
     * Get the list of attributes from a layer.
     * 
     * @param layer
     *            the layer to get the attributes from.
     * @param onlyNumeric
     *            if <code>true</code>, get only numeric attributes.
     * @return the array of field names.
     * @throws DataException
     */
    public static String[] getAttributesNames( FLyrVect layer, boolean onlyNumeric ) throws DataException {
        List<String> fieldNames = new ArrayList<>();
        DataStore dataStore = layer.getDataStore();
        if (dataStore instanceof FeatureStore) {
            FeatureStore featureStore = (FeatureStore) dataStore;
            FeatureType defaultFeatureType = featureStore.getDefaultFeatureType();
            FeatureAttributeDescriptor[] attributeDescriptors = defaultFeatureType.getAttributeDescriptors();
            for( int i = 0; i < attributeDescriptors.length; i++ ) {
                DataType dataType = attributeDescriptors[i].getDataType();
                if (onlyNumeric && !dataType.isNumeric()) {
                    continue;
                }
                fieldNames.add(attributeDescriptors[i].getName());
            }
        }
        return fieldNames.stream().toArray(String[]::new);
    }

}
