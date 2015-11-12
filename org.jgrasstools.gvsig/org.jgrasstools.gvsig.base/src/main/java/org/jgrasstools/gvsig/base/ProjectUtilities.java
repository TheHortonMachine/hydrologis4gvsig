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

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.PluginsLocator;
import org.gvsig.andami.PluginsManager;
import org.gvsig.andami.plugins.IExtension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.Document;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.coverage.store.parameter.RasterDataParameters;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemStoreParameters;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.dynobject.DynObject;
import org.opengis.referencing.FactoryException;

/**
 * Utilities to get project infos.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ProjectUtilities {

    private static PluginsManager pluginsManager = PluginsLocator.getManager();

    /**
     * Returns the file of a given plugin.
     * 
     * <p><b>This is the installation and might be readonly.</b>
     * 
     * @param pluginClass
     * @return the file to the plugin root.
     */
    public static File getPluginFolder( Class< ? extends IExtension> pluginClass ) {
        PluginServices plugin = pluginsManager.getPlugin(pluginClass);
        File pluginDirectory = plugin.getPluginDirectory();
        return pluginDirectory;
    }

    /**
     * Returns the file of a given plugin in the user's home folder.
     * 
     * <p><b>This should be used when writing is necessary.</b>
     * 
     * @param pluginClass
     * @return the file to the plugin root.
     */
    public static File getPluginHomeFolder( Class< ? extends IExtension> pluginClass ) {
        PluginServices plugin = pluginsManager.getPlugin(pluginClass);
        File pluginDirectory = plugin.getPluginHomeFolder();
        return pluginDirectory;
    }

    /**
     * Returns the folder of the gvSIG installation.
     * 
     * @return the folder of the installation root.
     */
    public static File getInstallationFolder() {
        File applicationFolder = pluginsManager.getApplicationFolder();
        return applicationFolder;
    }

    /**
     * Get the preferences of a certain plugin as defined in <b>resources-plugin/plugin-persistence.def</b>.
     * 
     * @param pluginClass the plugin class.
     * @return the preferences properties map.
     */
    public static DynObject getPluginPreferences( Class< ? extends IExtension> pluginClass ) {
        PluginServices pluginServices = pluginsManager.getPlugin(pluginClass);
        DynObject properties = pluginServices.getPluginProperties();
        return properties;
    }

    /**
     * Returns a file inside a given plugin.
     * 
     * <p><b>This is the installation and might be readonly.</b>
     * 
     * @param pluginClass
     * @return the file to the plugin root.
     */
    public static File getFileInPlugin( Class< ? extends IExtension> pluginClass, String relativePath ) {
        PluginServices plugin = pluginsManager.getPlugin(pluginClass);
        File pluginDirectory = plugin.getPluginDirectory();
        File file = new File(pluginDirectory, relativePath);
        return file;
    }

    /**
     * Get the current {@link MapContext}.
     * 
     * @return the current context or <code>null</code>.
     */
    public static MapContext getCurrentMapcontext() {
        ViewDocument viewDocument = getCurrentViewDocument();
        if (viewDocument != null) {
            MapContext mapContext = viewDocument.getMapContext();
            return mapContext;
        }
        return null;
    }

    /**
     * Get the current {@link MapControl}.
     * 
     * @return the current mapcontrol or <code>null</code>.
     */
    public static MapControl getCurrentMapcontrol() {
        IView view = getCurrentView();
        if (view != null) {
            MapControl mapControl = view.getMapControl();
            return mapControl;
        }
        return null;
    }

    /**
     * Get the current {@link IView}.
     * 
     * @return the current IView or <code>null</code>.
     */
    public static IView getCurrentView() {
        ViewDocument viewDocument = getCurrentViewDocument();
        if (viewDocument != null) {
            IWindow mainWindow = viewDocument.getMainWindow();
            if (mainWindow instanceof IView) {
                IView view = (IView) mainWindow;
                return view;
            }
        }
        return null;
    }

    /**
     * Get the current {@link ViewDocument}.
     * 
     * @return the current ViewDocument or <code>null</code>.
     */
    public static ViewDocument getCurrentViewDocument() {
        ApplicationManager applicationManager = ApplicationLocator.getManager();
        ProjectManager projectManager = applicationManager.getProjectManager();
        Document activeDocument = projectManager.getCurrentProject().getActiveDocument();
        ViewDocument viewDocument = null;
        if (activeDocument instanceof ViewDocument) {
            viewDocument = (ViewDocument) activeDocument;
            return viewDocument;
        }
        return null;
    }

}
