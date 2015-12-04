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
package org.jgrasstools.gvsig.pointinfo;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.PluginsLocator;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.DocumentManager;
import org.gvsig.app.project.documents.view.ViewManager;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Andami extension to generate tiles from a view.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PointInfoExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(PointInfoExtension.class);

    private static final String ACTION_POINTIFNO = "run-pointinfo-tool";

    private I18nManager i18nManager;

    private ApplicationManager applicationManager;

    private ProjectManager projectManager;

    private ThreadSafeDialogsManager dialogManager;

    private PointInfoController pointInfoController;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "copy", this);

        i18nManager = ToolsLocator.getI18nManager();
        applicationManager = ApplicationLocator.getManager();

        projectManager = applicationManager.getProjectManager();
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

    }

    public void postInitialize() {
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_POINTIFNO.equalsIgnoreCase(actionCommand)) {
            pointInfoController = new PointInfoController();
            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            windowManager.showWindow(pointInfoController.asJComponent(), "Simple Position Info Tool", MODE.WINDOW);
        }

    }

    /**
     * Check if tools of this extension are enabled.
     */
    public boolean isEnabled() {
        //
        // By default the tool is always enabled
        //
        return true;
    }

    /**
     * Check if tools of this extension are visible.
     */
    public boolean isVisible() {
        if (pointInfoController != null)
            pointInfoController.isVisibleTriggered();
        return true;
    }

}
