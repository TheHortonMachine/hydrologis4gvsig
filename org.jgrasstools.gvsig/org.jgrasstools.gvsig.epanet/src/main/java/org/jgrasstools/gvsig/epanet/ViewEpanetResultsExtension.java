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
package org.jgrasstools.gvsig.epanet;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.Document;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.epanet.core.ResultsPanel;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.EpanetFeatureTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Andami extension view epanet results.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ViewEpanetResultsExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(ViewEpanetResultsExtension.class);

    private static final String ACTION_VIEWRESULTS = "view-epanet-results";

    private I18nManager i18nManager;

    private ApplicationManager applicationManager;

    private ProjectManager projectManager;

    private ThreadSafeDialogsManager dialogManager;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "result", this);

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
        if (ACTION_VIEWRESULTS.equalsIgnoreCase(actionCommand)) {
            // Set the tool in the mapcontrol of the active view.

            IWindow activeWindow = applicationManager.getActiveWindow();
            if (activeWindow == null) {
                return;
            }
            try {
                /*
                 * TODO check if the active view is the right one
                 * and if the right layers are present.
                 */

                Document activeDocument = projectManager.getCurrentProject().getActiveDocument();
                ViewDocument view = null;
                if (activeDocument instanceof ViewDocument) {
                    view = (ViewDocument) activeDocument;
                    if (!view.getName().equals(i18nManager.getTranslation(CreateProjectFilesExtension.MY_VIEW_NAME))) {
                        dialogManager.messageDialog("Please select the Epanet Layer View to proceed.", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                FLayers layers = view.getMapContext().getLayers();

                SimpleFeatureCollection jFC = SyncEpanetShapefilesExtension.toFc(layers,
                        EpanetFeatureTypes.Junctions.ID.getName());
                SimpleFeatureCollection piFC = SyncEpanetShapefilesExtension.toFc(layers, EpanetFeatureTypes.Pipes.ID.getName());
                if (jFC == null || piFC == null) {
                    dialogManager.messageDialog(
                            "Could not find any pipes and junctions layer in the current view. Check your data.", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                File[] files = dialogManager.showOpenFileDialog("Select Epanet Results Database", JGTUtilities.getLastFile());
                if (files!=null) {
                    File file = files[0];
                    JGTUtilities.setLastPath(file.getAbsolutePath());
                    
                    final ResultsPanel resultsPanel = new ResultsPanel(file);
                    WindowManager windowManager = ToolsSwingLocator.getWindowManager();
                    windowManager.showWindow(resultsPanel.asJComponent(), "Epanet Results Browser", MODE.WINDOW);
                    
                    resultsPanel.addComponentListener(new ComponentListener(){
                        
                        public void componentShown( ComponentEvent e ) {
                            // TODO Auto-generated method stub
                            
                        }
                        
                        public void componentResized( ComponentEvent e ) {
                            // TODO Auto-generated method stub
                            
                        }
                        
                        public void componentMoved( ComponentEvent e ) {
                            // TODO Auto-generated method stub
                            
                        }
                        
                        public void componentHidden( ComponentEvent e ) {
                            // TODO Auto-generated method stub
                            
                        }
                    });
                    
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(resultsPanel);
                    parentFrame.addWindowListener(new WindowAdapter(){
                        @Override
                        public void windowClosed( WindowEvent e ) {
                            resultsPanel.freeResources();
                            super.windowClosed(e);
                        }
                    });
                }
                
                
                

            } catch (Exception e) {
                logger.error("ERROR", e);
                dialogManager.messageDialog("An error occurred while starting the Run Epanet wizard.", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
            }

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
        return true;
    }

}
