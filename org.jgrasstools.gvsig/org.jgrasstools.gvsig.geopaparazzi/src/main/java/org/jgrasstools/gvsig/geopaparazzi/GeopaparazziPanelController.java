package org.jgrasstools.gvsig.geopaparazzi;

import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.complexNotes2featurecollections;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.getGpsLogsList;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.getLogLinesFeatureCollection;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.media2IdBasedFeatureCollection;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.simpleNotes2featurecollection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoGpsLog.GpsLog;
import org.jgrasstools.gears.libs.exceptions.ModelsIllegalargumentException;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Geopaparazzi panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GeopaparazziPanelController extends GeopaparazziPanelView {

    public static final String FORM_NOTES_PREFIX = "Form Notes: ";

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(GeopaparazziPanelController.class);

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

    private LinkedHashMap<String, SimpleFeatureCollection> layerName2FCMap = new LinkedHashMap<String, SimpleFeatureCollection>();

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
        this.browseButton.addActionListener(new ActionListener(){

            public void actionPerformed( ActionEvent e ) {
                layerName2FCMap.clear();
                browse();
            }
        });
    }

    public void browse() {
        ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        File[] files = dialogsManager.showOpenFileDialog("Select Geopaparazzi File", JGTUtilities.getLastFile());
        if (files != null && files.length > 0) {
            File gpapFile = files[0];
            JGTUtilities.setLastPath(gpapFile.getAbsolutePath());
            this.geopaparazziDatabasePathField.setText(gpapFile.getAbsolutePath());

            try {
                openDatabaseFile(gpapFile);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void openDatabaseFile( File geopapDatabaseFile ) throws Exception {

        if (!geopapDatabaseFile.exists()) {
            throw new ModelsIllegalargumentException(
                    "The geopaparazzi database file (*.gpap) is missing. Check the inserted path.", this);
        }

        File outputFolderFile = geopapDatabaseFile.getParentFile();
        if (!outputFolderFile.exists()) {
            outputFolderFile.mkdirs();
        }
        // File mediaFolderFile = new
        // File(outputFolderFile,OmsGeopaparazzi4Converter.MEDIA_FOLDER_NAME);
        // mediaFolderFile.mkdir();
        // File chartsFolderFile = new File(outputFolderFile,
        // OmsGeopaparazzi4Converter.CHARTS_FOLDER_NAME);
        // chartsFolderFile.mkdir();

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + geopapDatabaseFile.getAbsolutePath());
        try {
            LinkedHashMap<String, String> metadataMap = OmsGeopaparazzi4Converter.getMetadataMap(connection);
            GeopaparazziMetadata model = new GeopaparazziMetadata(metadataMap);
            descriptionTable.setModel(model);

            LogProgressMonitor pm = new LogProgressMonitor();

            try {
                SimpleFeatureCollection notesFC = simpleNotes2featurecollection(connection, pm);
                layerName2FCMap.put("Simple Notes", notesFC);
            } catch (Exception e) {
                logger.error("Simple notes", e);
            }

            try {
                List<GpsLog> gpsLogsList = getGpsLogsList(connection);
                SimpleFeatureCollection logLinesFC = (SimpleFeatureCollection) getLogLinesFeatureCollection(pm, gpsLogsList);
                layerName2FCMap.put("GPS Logs", logLinesFC);
            } catch (Exception e) {
                logger.error("GPS Logs", e);
            }
            try {
                SimpleFeatureCollection mediaFC = media2IdBasedFeatureCollection(connection, pm);
                layerName2FCMap.put("Media Notes", mediaFC);
            } catch (Exception e) {
                logger.error("Media Notes", e);
            }
            try {
                HashMap<String, SimpleFeatureCollection> complexNotesFC = complexNotes2featurecollections(connection, pm);
                for( Entry<String, SimpleFeatureCollection> entry : complexNotesFC.entrySet() ) {
                    String key = entry.getKey();
                    layerName2FCMap.put(FORM_NOTES_PREFIX + key, entry.getValue());
                }
            } catch (Exception e) {
                logger.error("Form Notes", e);
            }

            DefaultListModel<String> layersModel = new DefaultListModel<String>();
            for( String layerName : layerName2FCMap.keySet() ) {
                layersModel.addElement(layerName);
            }
            geopaparazziLayersList.setModel(layersModel);
        } finally {
            if (connection != null)
                connection.close();
        }

        // DataManager dataManager = DALLocator.getDataManager();
        //
        // DataServerExplorerParameters parameters =
        // dataManager.createServerExplorerParameters("SqliteExplorer");
        // parameters.setDynValue("dbname", gpapFile.getAbsolutePath());
        // DataServerExplorer serverExplorer =
        // dataManager.openServerExplorer("SqliteExplorer",parameters);
        //
        // List list = serverExplorer.list();

    }

    public LinkedHashMap<String, SimpleFeatureCollection> getLayerName2FCMap() {
        return layerName2FCMap;
    }

}
