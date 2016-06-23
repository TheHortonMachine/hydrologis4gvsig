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
import java.util.function.Consumer;

import org.cresques.cts.IProjection;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.DataTypes;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.EditableFeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.EditableFeatureType;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureQuery;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.NewFeatureStoreParameters;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemServerExplorer;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemServerExplorerParameters;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.type.GeometryType;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.dispose.DisposeUtils;

/**
 * Feature utils methods.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FeatureUtilities {
    private static final String GEOMETRY_FIELD_NAME = "the_geom";

    private static final int PRECISION = 5;

    public static GeometryManager geometryManager = GeometryLocator.getGeometryManager();

    public static FeatureStore createFeatureStore( File outputFile, String[] fields, int[] fieldSizes, int[] dataTypes,
            GeometryType geometryType, IProjection projection ) throws Exception {

        DataManager dataManager = DALLocator.getDataManager();
        FilesystemServerExplorerParameters explorerParams = (FilesystemServerExplorerParameters) dataManager
                .createServerExplorerParameters(FilesystemServerExplorer.NAME);
        explorerParams.setRoot(outputFile.getParent());
        FilesystemServerExplorer filesystemServerExplorer = (FilesystemServerExplorer) dataManager
                .openServerExplorer("FilesystemExplorer", explorerParams);

        NewFeatureStoreParameters newFeatureStoreParameters = (NewFeatureStoreParameters) filesystemServerExplorer
                .getAddParameters(outputFile);
        newFeatureStoreParameters.setDynValue("CRS", projection);

        EditableFeatureType featureType = newFeatureStoreParameters.getDefaultFeatureType();

        for( int i = 0; i < fields.length; i++ ) {
            final EditableFeatureAttributeDescriptor efad = featureType.add(fields[i], dataTypes[i]);
            if (fieldSizes != null)
                efad.setSize(fieldSizes[i]);
            efad.setPrecision(PRECISION);
        }

        featureType.add(GEOMETRY_FIELD_NAME, DataTypes.GEOMETRY).setGeometryType(geometryType).setSRS(projection);
        featureType.setDefaultGeometryAttributeName(GEOMETRY_FIELD_NAME);

        newFeatureStoreParameters.setDefaultFeatureType(featureType);

        filesystemServerExplorer.add(newFeatureStoreParameters.getDataStoreName(), newFeatureStoreParameters, true);

        DataStoreParameters shpParams = DALLocator.getDataManager().createStoreParameters("Shape");
        shpParams.setDynValue("shpfile", outputFile);
        shpParams.setDynValue("CRS", projection);
        shpParams.validate();

        final DataManager manager = DALLocator.getDataManager();
        FeatureStore featureStore = (FeatureStore) manager.openStore(shpParams.getDataStoreName(), shpParams);
        // featureStore.edit(FeatureStore.MODE_APPEND);

        return featureStore;
    }

    /**
     * Add a new feature to a store based on passed objects.
     * 
     * @param featureStore the store.
     * @param geom the geometry.
     * @param fields the name of the fields.
     * @param values the values of the fields.
     * @throws Exception
     */
    public static void addFeature( FeatureStore featureStore, final Geometry geom, String[] fields, final Object[] values )
            throws Exception {
        final EditableFeature ef = featureStore.createNewFeature();
        for( int i = 0; i < fields.length; i++ ) {
            ef.set(fields[i], values[i]);
        }
        ef.set(GEOMETRY_FIELD_NAME, geom);
        featureStore.insert(ef);
    }

    /**
     * Removes all features from a store.
     * 
     * <p>The store needs to be in editing mode already.
     * 
     * @param featureStore the feature store.
     * @throws Exception
     */
    public static void deleteAllFeatures( FeatureStore featureStore ) throws Exception {
        FeatureSet waypointsSet = featureStore.getFeatureSet();
        DisposableIterator iterator = waypointsSet.fastIterator();
        try {
            while( iterator.hasNext() ) {
                iterator.next();
                iterator.remove();
            }
        } finally {
            DisposeUtils.dispose(iterator);
            DisposeUtils.dispose(waypointsSet);
        }
    }
    
    /**
     * Get all features from a featureStore as a list.
     * 
     * @param featureStore the store.
     * @param featureQuery an optional filter to apply.
     * @param consumer an optional consumer that takes a feature as input.
     * @return the list of features.
     * @throws Exception
     */
    public static List<Feature> getFeatures( FeatureStore featureStore, FeatureQuery featureQuery, Consumer<Feature> consumer )
            throws Exception {
        List<Feature> featuresList = new ArrayList<>();
        FeatureSet featureSet;
        if (featureQuery == null) {
            featureSet = featureStore.getFeatureSet();
        } else {
            featureSet = featureStore.getFeatureSet(featureQuery);
        }
        DisposableIterator featureIterator = featureSet.fastIterator();
        try {
            while( featureIterator.hasNext() ) {
                Feature feature = (Feature) featureIterator.next();
                if (consumer != null) {
                    consumer.accept(feature);
                }
                featuresList.add(feature.getCopy());
            }
        } finally {
            DisposeUtils.dispose(featureIterator);
            DisposeUtils.dispose(featureSet);
        }
        return featuresList;
    }

}
