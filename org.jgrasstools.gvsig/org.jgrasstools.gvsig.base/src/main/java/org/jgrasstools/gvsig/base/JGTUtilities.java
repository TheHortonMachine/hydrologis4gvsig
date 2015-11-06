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

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.PreferencesNode;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.coverage.store.parameter.RasterDataParameters;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.ProviderNotRegisteredException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemStoreParameters;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.opengis.referencing.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class JGTUtilities {
    private static final Logger logger = LoggerFactory.getLogger(JGTUtilities.class);

    public static final String LAST_PATH = "KEY_LAST_PATH";

    /**
     * Handle the last set path preference.
     * 
     * @return the last set path or the user home.
     */
    public static File getLastFile() {
        ApplicationManager applicationManager = ApplicationLocator.getManager();
        PreferencesNode preferences = applicationManager.getPreferences();
        String userHome = System.getProperty("user.home");
        String lastPath = preferences.get(LAST_PATH, userHome);
        File file = new File(lastPath);
        if (!file.exists()) {
            return new File(userHome);
        }
        return file;
    }

    /**
     * Save the passed path as last path available.
     * 
     * @param lastPath the last path to save.
     */
    public static void setLastPath( String lastPath ) {
        File file = new File(lastPath);
        if (!file.isDirectory()) {
            lastPath = file.getParentFile().getAbsolutePath();
        }
        ApplicationManager applicationManager = ApplicationLocator.getManager();
        PreferencesNode preferences = applicationManager.getPreferences();
        preferences.put(LAST_PATH, lastPath);
    }

    /**
     * Open the file as a feature store of type shape.
     *
     * @param shape file to be opened
     * @param epsgCode 
     *
     * @return the feature store
     */
    public static FeatureStore openShape( File shape, String epsgCode ) {
        try {
            DataManager manager = DALLocator.getDataManager();
            DataStoreParameters parameters = manager.createStoreParameters("Shape");
            parameters.setDynValue("shpfile", shape);
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

    public static void copyToClipboard( String text ) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public static void openFile( File file ) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(file);
        }
    }

}
