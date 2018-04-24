package org.hortonmachine.gvsig.geopaparazzi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.DefaultListModel;

import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.hortonmachine.dbs.compat.IHMConnection;
import org.hortonmachine.dbs.spatialite.hm.SqliteDb;
import org.hortonmachine.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.hortonmachine.gears.libs.exceptions.ModelsIllegalargumentException;
import org.hortonmachine.gears.libs.monitor.IHMProgressMonitor;
import org.hortonmachine.gears.libs.monitor.LogProgressMonitor;
import org.hortonmachine.gvsig.base.HMUtilities;
import org.hortonmachine.gvsig.base.utils.console.LogConsoleController;

/**
 * The Geopaparazzi panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GeopaparazziPanelController extends GeopaparazziPanelView {

    private static final long serialVersionUID = 1L;

    private ThreadSafeDialogsManager dialogManager;

    private File geopapDatabaseFile;

    private SqliteDb db;

    public GeopaparazziPanelController() {
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

        // dialogManager.messageDialog("Can't find any sqlite driver to open the database. Check
        // your settings.", "ERROR",
        // JOptionPane.ERROR_MESSAGE);
        // throw new ModelsIllegalargumentException("Can't find any sqlite driver. Check your
        // settings.", this);
        init();
    }

    private void init() {
        exportshapesCheckBox.setSelected(true);
        geopaparazziLabel.setText("GvSIG Mobile/Geopaparazzi database");
        this.browseButton.addActionListener(new ActionListener(){

            public void actionPerformed( ActionEvent e ) {
                browse();
            }
        });
    }

    public void browse() {
        ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        File[] files = dialogsManager.showOpenFileDialog("Select GvSIG Mobile/Geopaparazzi File", HMUtilities.getLastFile());
        if (files != null && files.length > 0) {
            final File gpapFile = files[0];
            HMUtilities.setLastPath(gpapFile.getAbsolutePath());
            this.geopaparazziDatabasePathField.setText(gpapFile.getAbsolutePath());

            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            IHMProgressMonitor pm = new LogProgressMonitor();
            final LogConsoleController logConsole = new LogConsoleController(pm);
            windowManager.showWindow(logConsole.asJComponent(), "GvSIG Mobile/Geopaparazzi data extraction", MODE.WINDOW);

            new Thread(new Runnable(){
                public void run() {
                    try {
                        logConsole.beginProcess("GvSIGMobileDataStore");
                        openDatabaseFile(gpapFile);
                        logConsole.finishProcess();
                        logConsole.stopLogging();
                        logConsole.setVisible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void openDatabaseFile( File geopapDatabaseFile ) throws Exception {

        this.geopapDatabaseFile = geopapDatabaseFile;
        if (!geopapDatabaseFile.exists()) {
            throw new ModelsIllegalargumentException(
                    "The GvSIG Mobile/Geopaparazzi database file (*.gpap) is missing. Check the inserted path.", this);
        }

        db = new SqliteDb();
        db.open(geopapDatabaseFile.getAbsolutePath());
        db.execOnConnection(databaseConnection -> {
            LinkedHashMap<String, String> metadataMap = GeopaparazziUtilities.getProjectMetadata(databaseConnection);
            GeopaparazziMetadata model = new GeopaparazziMetadata(metadataMap);
            descriptionTable.setModel(model);

            LogProgressMonitor pm = new LogProgressMonitor();
            pm.beginTask("Request layers...", IHMProgressMonitor.UNKNOWN);
            List<String> layerNamesList = GeopaparazziUtilities.getLayerNamesList(databaseConnection);
            DefaultListModel<String> layersModel = new DefaultListModel<String>();
            for( String layerName : layerNamesList ) {
                layersModel.addElement(layerName);
            }
            pm.done();
            geopaparazziLayersList.setModel(layersModel);
            return null;
        });
    }

    public SqliteDb getDb() {
        return db;
    }

    public File getGeopapDatabaseFile() {
        return geopapDatabaseFile;
    }

    public boolean doExportShps() {
        return exportshapesCheckBox.isSelected();
    }
}
