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
package org.jgrasstools.gvsig.geopaparazzi;

import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.complexNotes2featurecollections;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.getGpsLogsList;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.getLogLinesFeatureCollection;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.media2FeatureCollection;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.media2IdBasedFeatureCollection;
import static org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter.simpleNotes2featurecollection;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.gvsig.app.gui.WizardPanel;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorLegend;
import org.gvsig.fmap.mapcontext.rendering.legend.styling.ILabelingStrategy;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.marker.IMarkerSymbol;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoGpsLog.GpsLog;
import org.jgrasstools.gears.io.vectorwriter.OmsVectorWriter;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.StyleUtilities;
import org.jgrasstools.gvsig.base.utils.console.LogConsoleController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import wizard for geopap dbs.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GeopaparazziLayerWizard extends WizardPanel {
    private static final Logger logger = LoggerFactory.getLogger(GeopaparazziLayerWizard.class);
    private GeopaparazziPanelController controller;
    private File outputFolder;
    private File mediaFolder;
    private boolean doExportShps;
    private MapContextManager mapContextManager;

    @Override
    public void initWizard() {
        controller = new GeopaparazziPanelController();

        this.setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();

        constr.gridwidth = GridBagConstraints.RELATIVE;
        constr.gridheight = GridBagConstraints.RELATIVE;
        constr.fill = GridBagConstraints.BOTH;
        constr.anchor = GridBagConstraints.FIRST_LINE_START;
        constr.weightx = 1;
        constr.weighty = 1;
        add(controller, constr);

        setTabName("Geopaparazzi");
    }

    @Override
    public void execute() {
        WindowManager windowManager = ToolsSwingLocator.getWindowManager();
        final IJGTProgressMonitor pm = new LogProgressMonitor();
        final LogConsoleController logConsole = new LogConsoleController(pm);
        windowManager.showWindow(logConsole.asJComponent(), "Geopaparazzi data extraction", MODE.WINDOW);

        new Thread(new Runnable(){
            public void run() {
                try {
                    logConsole.beginProcess("GeopaparazziDataStore");
                    loadLayers(pm);
                    logConsole.finishProcess();
                    logConsole.stopLogging();
                    logConsole.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadLayers( IJGTProgressMonitor pm ) throws Exception {
        Connection connection = controller.getDatabaseConnection();
        File pluginFolder = ProjectUtilities.getPluginFolder(GenerateTilesExtension.class);
        doExportShps = controller.doExportShps();
        mapContextManager = MapContextLocator.getMapContextManager();

        if (doExportShps) {
            File geopapDatabaseFile = controller.getGeopapDatabaseFile();
            // create needed folders
            String dbName = FileUtilities.getNameWithoutExtention(geopapDatabaseFile);
            outputFolder = new File(geopapDatabaseFile.getParentFile(), dbName);
            mediaFolder = new File(outputFolder, "media");
            if (!outputFolder.exists()) {
                outputFolder.mkdir();
            }
            if (!mediaFolder.exists()) {
                mediaFolder.mkdir();
            }
            if (!mediaFolder.exists()){
                doExportShps = false;
            }
        }

        try {
            if (connection == null) {
                return;
            }
            String logsName = "gps_logs";
            try {
                List<GpsLog> gpsLogsList = getGpsLogsList(connection);
                SimpleFeatureCollection logLinesFC = (SimpleFeatureCollection) getLogLinesFeatureCollection(pm, gpsLogsList);
                IVectorLegend leg = StyleUtilities.createSimpleLineLegend(Color.BLACK, 3, 190);
                File gpsLogsLabelingFile = new File(pluginFolder, "styles/gps_logs_labels.gvslab");
                ILabelingStrategy labelingStrategy = StyleUtilities.getLabelsFromFile(gpsLogsLabelingFile);
                addLayer(logsName, logLinesFC, leg, labelingStrategy);
            } catch (Exception e) {
                logger.error(logsName, e);
            }

            String notesName = "simple_notes";
            try {
                SimpleFeatureCollection notesFC = simpleNotes2featurecollection(connection, pm);
                IVectorLegend leg = StyleUtilities.createSimplePointLegend(IMarkerSymbol.CIRCLE_STYLE, 15, Color.RED, 128,
                        Color.BLACK, 1.0);
                File simpleNotesLabelingFile = new File(pluginFolder, "styles/simple_notes_labels.gvslab");
                ILabelingStrategy labelingStrategy = StyleUtilities.getLabelsFromFile(simpleNotesLabelingFile);
                addLayer(notesName, notesFC, leg, labelingStrategy);
            } catch (Exception e) {
                logger.error(notesName, e);
            }

            String mediaName = "media_notes";
            try {
                SimpleFeatureCollection mediaFC;
                if (doExportShps) {
                    mediaFC = media2FeatureCollection(connection, mediaFolder, pm);
                } else {
                    mediaFC = media2IdBasedFeatureCollection(connection, pm);
                }
                IVectorLegend leg = StyleUtilities.createSimplePointLegend(IMarkerSymbol.SQUARE_STYLE, 15, Color.BLUE, 128,
                        Color.BLUE, 1.0);
                File mdeiaNotesLabelingFile;
                if (doExportShps) {
                    mdeiaNotesLabelingFile = new File(pluginFolder, "styles/media_notes_path_labels.gvslab");
                } else {
                    mdeiaNotesLabelingFile = new File(pluginFolder, "styles/media_notes_id_labels.gvslab");
                }
                ILabelingStrategy labelingStrategy = StyleUtilities.getLabelsFromFile(mdeiaNotesLabelingFile);
                addLayer(mediaName, mediaFC, leg, labelingStrategy);
            } catch (Exception e) {
                logger.error(mediaName, e);
            }
            try {

                Color[] colors = new Color[]{Color.GREEN, //
                        Color.MAGENTA, //
                        Color.ORANGE, //
                        Color.CYAN, //
                        Color.LIGHT_GRAY, //
                        Color.YELLOW, //
                        Color.WHITE, //
                        Color.PINK//
                };

                HashMap<String, SimpleFeatureCollection> complexNotesFC = complexNotes2featurecollections(connection, pm);
                int colorIndex = 0;
                for( Entry<String, SimpleFeatureCollection> entry : complexNotesFC.entrySet() ) {
                    String key = entry.getKey();
                    Color color = colors[colorIndex];
                    IVectorLegend leg = StyleUtilities.createSimplePointLegend(IMarkerSymbol.TRIANGLE_STYLE, 15, color, 128,
                            Color.BLACK, 1.0);
                    colorIndex++;
                    if (colorIndex == colors.length) {
                        colorIndex = 0;
                    }
                    addLayer(key, entry.getValue(), leg, null);
                }
            } catch (Exception e) {
                logger.error("Form Notes", e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.close();
        }
    }

    private void addLayer( String layerName, SimpleFeatureCollection data, IVectorLegend legend,
            ILabelingStrategy labelingStrategy ) throws Exception {
        FeatureStore featureStore;
        if (doExportShps) {
            // dump to disk
            File shpFile = new File(outputFolder, layerName + ".shp");
            OmsVectorWriter.writeVector(shpFile.getAbsolutePath(), data);
            featureStore = JGTUtilities.openShape(shpFile, "EPSG:4326");
        } else {
            featureStore = GtGvsigConversionUtilities.toGvsigMemoryFeatureStore(data);
        }
        FLyrVect layer = (FLyrVect) mapContextManager.createLayer(layerName, featureStore);
        if (legend != null) {
            layer.setLegend(legend);
        }

        if (labelingStrategy != null) {
            layer.setLabelingStrategy(labelingStrategy);
            layer.setIsLabeled(true);
        }

        this.getMapContext().getLayers().addLayer(layer);
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public DataStoreParameters[] getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

}
