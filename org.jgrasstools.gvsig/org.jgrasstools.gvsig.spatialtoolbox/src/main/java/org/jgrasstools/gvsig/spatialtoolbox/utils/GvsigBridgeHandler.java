package org.jgrasstools.gvsig.spatialtoolbox.utils;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.cresques.cts.IProjection;
import org.gvsig.fmap.geom.primitive.Point;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.tools.dynobject.DynObject;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gui.utils.GuiBridgeHandler;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.JGrasstoolsExtension;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.spatialtoolbox.SpatialtoolboxExtension;

@SuppressWarnings({"unchecked", "rawtypes"})
public class GvsigBridgeHandler implements GuiBridgeHandler {

    private ThreadSafeDialogsManager dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

    @Override
    public void messageDialog( String arg0, String arg1, int arg2 ) {
        dialogManager.messageDialog(arg0, arg1, arg2);
    }

    @Override
    public void messageDialog( String arg0, String[] arg1, String arg2, int arg3 ) {
        dialogManager.messageDialog(arg0, arg1, arg2, arg3);
    }

    @Override
    public void setLibsFolder( File arg0 ) {
        // not used
    }

    @Override
    public File getLibsFolder() {
        File jgtLibsFolder = ProjectUtilities.getFileInPlugin(JGrasstoolsExtension.class, "lib");
        return jgtLibsFolder;
    }

    @Override
    public Point2D getWorldPoint( int x, int y ) {
        MapControl mapControl = ProjectUtilities.getCurrentMapcontrol();
        if (mapControl != null) {
            Point mapPoint = mapControl.getViewPort().convertToMapPoint(new Point2D.Double(x, y));
            return new Point2D.Double(mapPoint.getX(), mapPoint.getY());
        }
        return null;
    }

    @Override
    public HashMap<String, String> getSpatialToolboxPreferencesMap() {
        DynObject preferences = ProjectUtilities.getPluginPreferences(SpatialtoolboxExtension.class);
        Object prefsMapTmp = preferences.getDynValue(SPATIAL_TOOLBOX_PREFERENCES_KEY);
        if (prefsMapTmp != null) {
            HashMap<String, String> prefsMap = (HashMap) prefsMapTmp;
            return prefsMap;
        }
        return new HashMap<>();
    }

    @Override
    public void setSpatialToolboxPreferencesMap( HashMap<String, String> prefsMap ) {
        DynObject preferences = ProjectUtilities.getPluginPreferences(SpatialtoolboxExtension.class);
        preferences.setDynValue(SPATIAL_TOOLBOX_PREFERENCES_KEY, prefsMap);
    }

    @Override
    public String promptForCrs() {
        IProjection projection = JGTUtilities.openCrsDialog();
        if (projection != null) {
            String epsg = projection.getAbrev();
            return epsg;
        }
        return null;
    }

    @Override
    public File[] showOpenDirectoryDialog( String arg0, File arg1 ) {
        return dialogManager.showOpenDirectoryDialog(arg0, arg1);
    }

    @Override
    public File[] showOpenFileDialog( String arg0, File arg1 ) {
        return dialogManager.showOpenFileDialog(arg0, arg1);
    }

    @Override
    public File[] showSaveFileDialog( String arg0, File arg1 ) {
        return dialogManager.showSaveFileDialog(arg0, arg1);
    }

    @Override
    public JFrame showWindow( JComponent arg0, String arg1 ) {
        WindowManager windowManager = ToolsSwingLocator.getWindowManager();
        windowManager.showWindow(arg0, arg1, MODE.WINDOW);
        return null;
    }

    @Override
    public boolean supportsMapContext() {
        return true;
    }

}
