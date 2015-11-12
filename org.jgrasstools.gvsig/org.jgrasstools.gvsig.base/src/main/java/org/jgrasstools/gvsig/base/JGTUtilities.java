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

import javax.swing.JComponent;

import org.cresques.cts.IProjection;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.PreferencesNode;
import org.gvsig.app.gui.panels.CRSSelectPanelFactory;
import org.gvsig.app.gui.panels.crs.ISelectCrsPanel;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.ProviderNotRegisteredException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
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

    /**
     * Zoom to a given Envelope.
     * 
     * @param envelope the envelope to zoom to.
     */
    public static void zoomTo( Envelope envelope ) {
        MapContext mapContext = ProjectUtilities.getCurrentMapcontext();
        if (mapContext != null) {
            mapContext.getViewPort().setEnvelope(envelope);
            mapContext.invalidate();
        }
    }

    /**
     * Open a dialog to ask the user for a projection.
     * 
     * @return teh choosen {@link IProjection} or <code>null</code>.
     */
    public static IProjection openCrsDialog() {
        ISelectCrsPanel csSelect = CRSSelectPanelFactory.getUIFactory().getSelectCrsPanel(null, true);
        ToolsSwingLocator.getWindowManager().showWindow((JComponent) csSelect, "Select a projection.", MODE.DIALOG);
        if (csSelect.isOkPressed() && csSelect.getProjection() != null) {
            IProjection prj = csSelect.getProjection();
            return prj;
        } else {
            return null;
        }
    }

}
