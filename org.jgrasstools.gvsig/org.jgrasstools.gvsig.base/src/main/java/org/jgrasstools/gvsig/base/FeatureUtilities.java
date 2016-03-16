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

import org.cresques.cts.IProjection;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.DataTypes;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.EditableFeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.EditableFeatureType;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.NewFeatureStoreParameters;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemServerExplorer;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemServerExplorerParameters;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.type.GeometryType;

/**
 * Feature utils methods.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FeatureUtilities {
    private static final String GEOMETRY_FIELD_NAME = "the_geom";

    private static final int PRECISION = 5;

    private static GeometryManager geometryManager = GeometryLocator.getGeometryManager();

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

    public void addFeature( FeatureStore featureStore, final Geometry geom, String[] fields, final Object[] values )
            throws Exception {
        final EditableFeature ef = featureStore.createNewFeature();
        for( int i = 0; i < fields.length; i++ ) {
            ef.set(fields[i], values[i]);
        }
        ef.set(GEOMETRY_FIELD_NAME, geom);
        featureStore.insert(ef);
    }

}
