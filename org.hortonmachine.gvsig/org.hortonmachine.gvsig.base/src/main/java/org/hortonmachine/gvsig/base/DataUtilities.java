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
package org.hortonmachine.gvsig.base;

import java.io.File;

import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataParameters;
import org.gvsig.fmap.dal.DataStore;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.ProviderNotRegisteredException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to handle data.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class DataUtilities {
    private static final Logger logger = LoggerFactory.getLogger(DataUtilities.class);

    public static final String TIFF = "tiff";
    public static final String TIF = "tif";
    public static final String ASC = "asc";
    public static final String SHP = "shp";

    public static final String[] supportedVectors = {SHP};
    public static final String[] supportedRasters = {ASC, TIF, TIFF};

    /**
     * Checks a given name of a file if it is a supported vector extension.
     * 
     * @param name the name of the file.
     * @return <code>true</code>, if the extension is supported.
     */
    public static boolean isSupportedVectorExtension( String name ) {
        for( String ext : supportedVectors ) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks a given name of a file if it is a supported raster extension.
     * 
     * @param name the name of the file.
     * @return <code>true</code>, if the extension is supported.
     */
    public static boolean isSupportedRasterExtension( String name ) {
        for( String ext : supportedRasters ) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Open the file as a feature store of type shape.
     *
     * @param shapeFile file to be opened
     * @param epsgCode 
     *
     * @return the feature store
     */
    public static FeatureStore getShapefileDatastore( File shapeFile, String epsgCode ) {
        try {
            DataManager manager = DALLocator.getDataManager();
            DataStoreParameters parameters = manager.createStoreParameters("Shape");
            parameters.setDynValue("shpfile", shapeFile);
            parameters.setDynValue("crs", epsgCode);
            return (FeatureStore) manager.openStore("Shape", parameters);
        } catch (InitializeException e) {
            logger.error(e.getMessageStack());
            throw new RuntimeException(e);
        } catch (ProviderNotRegisteredException e) {
            logger.error(e.getMessageStack());
            throw new RuntimeException(e);
        } catch (ValidateDataParametersException e) {
            logger.error(e.getMessageStack());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Open a raster file source and get a RasterDataStore.
     * 
     * TODO to be checked, copied from outdated docs
     * 
     * @param source the raster file.
     * @return the datastore.
     * @throws Exception
     */
    public static DataStore getRasterDatastore( File source ) throws Exception {
        DataManager manager = DALLocator.getDataManager();
        DataParameters parameters = manager.createStoreParameters("Gdal Store");
        parameters.setDynValue("uri", source.toURI());
        DataStore store = manager.openStore("Gdal Store", parameters);
        return store;
    }
}
