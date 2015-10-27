/**
 * gvSIG. Desktop Geographic Information System.
 *
 * Copyright (C) 2007-2012 gvSIG Association.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * For any additional information, do not hesitate to contact us at info AT
 * gvsig.com, or visit our website www.gvsig.com.
 */
package org.jgrasstools.gvsig.epanet;

import java.awt.GridBagConstraints;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.app.project.documents.view.ViewManager;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.crs.CRSFactory;
import org.gvsig.fmap.mapcontext.exceptions.LoadLayerException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.tools.Behavior.PointBehavior;
import org.gvsig.landregistryviewer.LandRegistryViewerLocator;
import org.gvsig.landregistryviewer.LandRegistryViewerManager;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.EpanetProjectFilesGenerator;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.EpanetFeatureTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.hydrologis.jgrass.epanet.EpanetPlugin;
import eu.hydrologis.jgrass.epanet.actions.CoordinateReferenceSystem;
import eu.hydrologis.jgrass.epanet.actions.DirectoryDialog;
import eu.hydrologis.jgrass.epanet.actions.EclipseProgressMonitorAdapter;
import eu.hydrologis.jgrass.epanet.actions.IMap;
import eu.hydrologis.jgrass.epanet.actions.IRunnableWithProgress;
import eu.hydrologis.jgrass.epanet.utils.EpanetUtilities;

/**
 * Andami extension to show LandRegistryViewer in the application.
 *
 * @author gvSIG Team
 * @version $Id$
 */
public class CreateProjectFilesExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(CreateProjectFilesExtension.class);

    private static final String MY_VIEW_NAME = "Epanet Layer View";

    private static final String ACTION_CREATEPROJECTFILES = "create epanet project files";


    private IView viewWindow;

    public void initialize() {
        IconThemeHelper.registerIcon("action", "new-project", this);
    }

    public void postInitialize() {
        viewWindow = this.createViewWindow();
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_CREATEPROJECTFILES.equalsIgnoreCase(actionCommand)) {
            // Set the tool in the mapcontrol of the active view.

            ApplicationManager application = ApplicationLocator.getManager();
            if (application.getActiveWindow() != viewWindow) {
                return;
            }
            
            DirectoryDialog folderDialog = new DirectoryDialog(window.getShell(), SWT.OPEN);
            folderDialog.setText("Choose a folder into which to create the project files");
            String selpath = folderDialog.open();
            if (selpath == null || selpath.length() < 1) {
                return;
            }
            File file = new File(selpath);
            generateProjectShapefiles(file);
            
        }
    }
    
    public void generateProjectShapefiles( final File baseFolder ) {
        IMap activeMap = ApplicationGIS.getActiveMap();
        final CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();

        IRunnableWithProgress operation = new IRunnableWithProgress(){
            public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
                EclipseProgressMonitorAdapter pm = new EclipseProgressMonitorAdapter(monitor);
                try {
                    String code = null;
                    try {
                        Integer epsg = CRS.lookupEpsgCode(mapCrs, true);
                        code = "EPSG:" + epsg;
                    } catch (Exception e) {
                        // try non epsg
                        code = CRS.lookupIdentifier(mapCrs, true);
                    }

                    EpanetProjectFilesGenerator gen = new EpanetProjectFilesGenerator();
                    gen.pm = pm;
                    gen.pCode = code;

                    gen.inFolder = baseFolder.getAbsolutePath();
                    gen.process();

                    /*
                     * copy also style files over
                     */
                    URL stylesFolderUrl = Platform.getBundle(EpanetPlugin.PLUGIN_ID).getResource("styles");
                    String stylesFolderPath = FileLocator.toFileURL(stylesFolderUrl).getPath();
                    File stylesFolderFile = new File(stylesFolderPath);
                    File[] styleFiles = stylesFolderFile.listFiles();
                    for( File styleFile : styleFiles ) {
                        FileUtils.copyFileToDirectory(styleFile, baseFolder);
                    }

                    String jPath = baseFolder.getAbsolutePath() + File.separator
                            + EpanetFeatureTypes.Junctions.ID.getShapefileName();
                    EpanetUtilities.addServiceToCatalogAndMap(jPath, true, true, monitor);
                    String tPath = baseFolder.getAbsolutePath() + File.separator + EpanetFeatureTypes.Tanks.ID.getShapefileName();
                    EpanetUtilities.addServiceToCatalogAndMap(tPath, true, true, monitor);
                    String piPath = baseFolder.getAbsolutePath() + File.separator
                            + EpanetFeatureTypes.Pipes.ID.getShapefileName();
                    EpanetUtilities.addServiceToCatalogAndMap(piPath, true, true, monitor);
                    String puPath = baseFolder.getAbsolutePath() + File.separator
                            + EpanetFeatureTypes.Pumps.ID.getShapefileName();
                    EpanetUtilities.addServiceToCatalogAndMap(puPath, true, true, monitor);
                    String vPath = baseFolder.getAbsolutePath() + File.separator
                            + EpanetFeatureTypes.Valves.ID.getShapefileName();
                    EpanetUtilities.addServiceToCatalogAndMap(vPath, true, true, monitor);
                    String rPath = baseFolder.getAbsolutePath() + File.separator
                            + EpanetFeatureTypes.Reservoirs.ID.getShapefileName();
                    EpanetUtilities.addServiceToCatalogAndMap(rPath, true, true, monitor);

                } catch (Exception e) {
                    String message = "An error occurred while creating the project files.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, EpanetPlugin.PLUGIN_ID, e);
                    e.printStackTrace();
                } finally {
                    pm.done();
                }
            }
        };
        PlatformGIS.runInProgressDialog("Create project shapefiles...", true, operation, true);

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
        //
        // The tool is visible only when our view is active
        //

        ApplicationManager application = ApplicationLocator.getManager();

        return application.getActiveWindow() == viewWindow;
    }

    /**
     * Create the view in the project. Add the blocks layer to the view, and
     * register the tool for get info of the blocks.
     */
    private IView createViewWindow() throws LoadLayerException {
        I18nManager i18nManager = ToolsLocator.getI18nManager();
        ApplicationManager application = ApplicationLocator.getManager();

        ProjectManager projectManager = application.getProjectManager();

        // 1. Create a new view and set the name.
        ViewManager viewManager = (ViewManager) projectManager.getDocumentManager(ViewManager.TYPENAME);
        ViewDocument view = (ViewDocument) viewManager.createDocument();
        view.setName(i18nManager.getTranslation(MY_VIEW_NAME));

        // Setting view's projection to shapefile's known CRS
        view.getMapContext().setProjection(CRSFactory.getCRS("EPSG:23030"));

        // 2. Create a new layer with the blocks
        FLyrVect layer = (FLyrVect) application.getMapContextManager().createLayer(i18nManager.getTranslation("_Blocks"),
                this.manager.getBlocks());

        // Add a new property to the layer to identify it.
        layer.setProperty("ViewerLayer", Boolean.TRUE);

        // 3. Add this layer to the mapcontext of the new view.
        view.getMapContext().getLayers().addLayer(layer);

        // 4. Add the view to the current project.
        projectManager.getCurrentProject().add(view);

        // 5. Force to show the view's window.
        IView viewWindow = (IView) viewManager.getMainWindow(view);
        application.getUIManager().addWindow(viewWindow, GridBagConstraints.CENTER);
        try {
            // 6. and maximise the window
            application.getUIManager().setMaximum((IWindow) viewWindow, true);
        } catch (PropertyVetoException e) {
            logger.info("Can't maximize view.", e);
        }

        // 7. Register the tool in the mapcontrol of the view to listen to point selections.
        ParcelsOfBlockListener listener = new ParcelsOfBlockListener();
        viewWindow.getMapControl().addBehavior(TOOL_NAME, new PointBehavior(listener));

        return viewWindow;
    }

    /**
     * Get a resource as a File from a path name in the class path.
     *
     * @param pathname
     *
     * @return resource as a File
     */
    private File getResource( String pathname ) {
        URL res = this.getClass().getClassLoader().getResource(pathname);
        return new File(res.getPath());
    }

    public boolean isMyLayerActive() {
        ApplicationManager application = ApplicationLocator.getManager();

        if (application.getActiveWindow() != viewWindow) {
            return false;
        }
        ViewDocument viewDoc = this.viewWindow.getViewDocument();
        FLayer[] activeLayers = viewDoc.getMapContext().getLayers().getActives();
        for( int i = 0; i < activeLayers.length; i++ ) {
            boolean viewerLayer = BooleanUtils.isTrue((Boolean) activeLayers[i].getProperty("ViewerLayer"));
            if (viewerLayer) {
                return true;
            }
        }
        return false;
    }

}
