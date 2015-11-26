package org.jgrasstools.gvsig.prjtools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.cresques.cts.IProjection;
import org.gvsig.app.gui.panels.CRSSelectPanelFactory;
import org.gvsig.app.gui.panels.crs.ISelectCrsPanel;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class PrjToolsController extends PrjToolsView implements Component {

    private HashMap<String, File> layersFilesMap;
    private MapContext currentMapcontext;
    private ThreadSafeDialogsManager dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

    private String selectedLayer;

    public PrjToolsController() {
        setPreferredSize(new Dimension(600, 300));

        init();
    }

    private void init() {
        setCombos();

        layerToPrjCombo.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                selectedLayer = (String) layerToPrjCombo.getSelectedItem();
            }
        });

        addComponentListener(new ComponentListener(){

            public void componentShown( ComponentEvent e ) {
            }

            public void componentResized( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentHidden( ComponentEvent e ) {
                freeResources();
            }
        });

        choosePrjButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {

                ISelectCrsPanel csSelect = CRSSelectPanelFactory.getUIFactory().getSelectCrsPanel(null, true);
                ToolsSwingLocator.getWindowManager().showWindow((JComponent) csSelect,
                        "Please insert the CRS EPSG code for the required projection.", MODE.DIALOG);
                if (csSelect.isOkPressed() && csSelect.getProjection() != null) {
                    IProjection selectedProjection = csSelect.getProjection();
                    try {
                        CoordinateReferenceSystem crs = GtGvsigConversionUtilities.gvsigCrs2gtCrs(selectedProjection);
                        String crsWkt = crs.toWKT();
                        customPrjArea.setText(crsWkt);
                    } catch (FactoryException e1) {
                        e1.printStackTrace();
                    }
                }

            }
        });

        applyPrjButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                if (selectedLayer != null) {
                    String crsWktText = customPrjArea.getText();

                    File file = layersFilesMap.get(selectedLayer);
                    if (file != null && file.exists()) {
                        String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
                        File prjFile = new File(file.getParentFile(), nameWithoutExtention + ".prj");
                        try {
                            if (prjFile.exists()) {
                                int answer = dialogManager.confirmDialog(
                                        "This will overwrite an existing prj file. Do you want to continue?", "WARNING",
                                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (answer == JOptionPane.YES_OPTION) {
                                    writePrj(crsWktText, prjFile);
                                }
                            } else {
                                writePrj(crsWktText, prjFile);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            dialogManager.messageDialog("An error occurred wile creating the prj file: " + e1.getMessage(),
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }

            private void writePrj( String crsWktText, File prjFile ) throws IOException {
                FileUtilities.writeFile(crsWktText, prjFile);
                dialogManager.messageDialog("Prj file written to: " + prjFile.getAbsolutePath(), "INFO",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

    }

    protected void freeResources() {
    }

    private void setCombos() {

        try {
            Object selectedLayer = layerToPrjCombo.getSelectedItem();

            String[] fileBasedLayers = getFileBasedLayers();
            layerToPrjCombo.setModel(new DefaultComboBoxModel<String>(fileBasedLayers));

            if (selectedLayer != null) {
                layerToPrjCombo.setSelectedItem(selectedLayer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JComponent asJComponent() {
        return this;
    }

    public void isVisibleTriggered() {
        // MapContext newMapcontext = ProjectUtilities.getCurrentMapcontext();
        // if (newMapcontext == currentMapcontext) {
        // return;
        // }
        setCombos();
    }

    private String[] getFileBasedLayers() throws Exception {
        layersFilesMap = new HashMap<String, File>();

        currentMapcontext = ProjectUtilities.getCurrentMapcontext();
        if (currentMapcontext == null) {
            return new String[0];
        }
        List<FLyrRaster> rasterLayers = LayerUtilities.getRasterLayers(currentMapcontext);
        List<FLyrVect> vectorLayers = LayerUtilities.getVectorLayers(currentMapcontext);

        List<FLayer> fileBasedLayers = new ArrayList<>();
        for( FLyrRaster fLyrRaster : rasterLayers ) {
            File file = LayerUtilities.getFileFromRasterFileLayer(fLyrRaster);
            if (file != null && file.exists()) {
                fileBasedLayers.add(fLyrRaster);
                layersFilesMap.put(fLyrRaster.getName(), file);
            }
        }
        for( FLyrVect fLyrVect : vectorLayers ) {
            File file = LayerUtilities.getFileFromVectorFileLayer(fLyrVect);
            if (file != null && file.exists()) {
                fileBasedLayers.add(fLyrVect);
                layersFilesMap.put(fLyrVect.getName(), file);
            }
        }
        String[] layerNames = new String[fileBasedLayers.size()];
        for( int i = 0; i < layerNames.length; i++ ) {
            layerNames[i] = fileBasedLayers.get(i).getName();
        }
        Arrays.sort(layerNames);
        return layerNames;
    }

}
