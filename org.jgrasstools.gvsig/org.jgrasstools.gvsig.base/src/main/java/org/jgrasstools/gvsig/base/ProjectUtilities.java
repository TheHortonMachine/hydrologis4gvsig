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

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.PluginsLocator;
import org.gvsig.andami.PluginsManager;
import org.gvsig.andami.plugins.IExtension;
import org.gvsig.tools.dynobject.DynObject;

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

}
