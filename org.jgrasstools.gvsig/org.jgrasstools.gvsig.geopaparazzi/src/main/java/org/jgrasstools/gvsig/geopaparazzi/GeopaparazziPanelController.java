package org.jgrasstools.gvsig.geopaparazzi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;
import org.jgrasstools.gears.libs.exceptions.ModelsIllegalargumentException;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.utils.console.LogConsoleController;

/**
 * The Geopaparazzi panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GeopaparazziPanelController extends GeopaparazziPanelView {

    private static final long serialVersionUID = 1L;

    private static boolean hasDriver = false;

    static {
        try {
            // make sure sqlite driver are there
            Class.forName("org.sqlite.JDBC");
            hasDriver = true;
        } catch (Exception e) {
        }
    }

    private ThreadSafeDialogsManager dialogManager;

    private Connection databaseConnection;

    private File geopapDatabaseFile;

    public GeopaparazziPanelController() {
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

        if (!hasDriver) {
            dialogManager.messageDialog("Can't find any sqlite driver to open the database. Check your settings.", "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            // throw new ModelsIllegalargumentException("Can't find any sqlite driver. Check your
            // settings.", this);
        } else {
            init();
        }
    }

    private void init() {
        exportshapesCheckBox.setSelected(true);
        this.browseButton.addActionListener(new ActionListener(){

            public void actionPerformed( ActionEvent e ) {
                browse();
            }
        });
    }

    public void browse() {
        ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        File[] files = dialogsManager.showOpenFileDialog("Select Geopaparazzi File", JGTUtilities.getLastFile());
        if (files != null && files.length > 0) {
            final File gpapFile = files[0];
            JGTUtilities.setLastPath(gpapFile.getAbsolutePath());
            this.geopaparazziDatabasePathField.setText(gpapFile.getAbsolutePath());

            WindowManager windowManager = ToolsSwingLocator.getWindowManager();
            IJGTProgressMonitor pm = new LogProgressMonitor();
            final LogConsoleController logConsole = new LogConsoleController(pm);
            windowManager.showWindow(logConsole.asJComponent(), "Geopaparazzi data extraction", MODE.WINDOW);

            new Thread(new Runnable(){
                public void run() {
                    try {
                        logConsole.beginProcess("GeopaparazziDataStore");
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
                    "The geopaparazzi database file (*.gpap) is missing. Check the inserted path.", this);
        }

        databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + geopapDatabaseFile.getAbsolutePath());
        LinkedHashMap<String, String> metadataMap = OmsGeopaparazzi4Converter.getMetadataMap(databaseConnection);
        GeopaparazziMetadata model = new GeopaparazziMetadata(metadataMap);
        descriptionTable.setModel(model);

        LogProgressMonitor pm = new LogProgressMonitor();
        pm.beginTask("Request layers...", IJGTProgressMonitor.UNKNOWN);
        List<String> layerNamesList = OmsGeopaparazzi4Converter.getLayerNamesList(databaseConnection);
        DefaultListModel<String> layersModel = new DefaultListModel<String>();
        for( String layerName : layerNamesList ) {
            layersModel.addElement(layerName);
        }
        pm.done();
        geopaparazziLayersList.setModel(layersModel);
    }

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }

    public File getGeopapDatabaseFile() {
        return geopapDatabaseFile;
    }

    public boolean doExportShps() {
        return exportshapesCheckBox.isSelected();
    }
}
