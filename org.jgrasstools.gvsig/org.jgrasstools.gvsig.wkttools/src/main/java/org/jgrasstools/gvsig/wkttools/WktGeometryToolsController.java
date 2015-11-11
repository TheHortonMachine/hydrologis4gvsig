package org.jgrasstools.gvsig.wkttools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.GeometryException;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.exception.CreateGeometryException;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayerStatus;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;

/**
 * WKT Geometry tool gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class WktGeometryToolsController extends WktGeometryToolsView implements Component {
    private ThreadSafeDialogsManager dialogManager;

    public WktGeometryToolsController() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(500, 350));

        getWktFromLayerArea.setLineWrap(true);
        putWktToLayerArea.setLineWrap(true);

        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        ImageIcon copyIcon = IconThemeHelper.getImageIcon("copy");
        copyWktButton.setIcon(copyIcon);
        copyWktButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                String wktText = getWktFromLayerArea.getText();
                if (wktText.trim().length() != 0)
                    JGTUtilities.copyToClipboard(wktText);
            }
        });
        getWktFromLayerButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                MapContext mapcontext = ProjectUtilities.getCurrentMapcontext();
                try {
                    if (mapcontext != null) {
                        StringBuilder sb = new StringBuilder();
                        FLayers layers = mapcontext.getLayers();
                        int layersCount = layers.getLayersCount();
                        for( int i = 0; i < layersCount; i++ ) {
                            FLayer layer = layers.getLayer(i);
                            if (layer instanceof FLyrVect) {
                                FLyrVect vectorLayer = (FLyrVect) layer;
                                FLayerStatus fLayerStatus = vectorLayer.getFLayerStatus();
                                if (fLayerStatus.active && fLayerStatus.visible) {
                                    FeatureSelection featureSelection = vectorLayer.getFeatureStore().getFeatureSelection();
                                    DisposableIterator fastIterator = featureSelection.fastIterator();
                                    while( fastIterator.hasNext() ) {
                                        Feature feature = (Feature) fastIterator.next();
                                        Geometry geometry = feature.getDefaultGeometry();
                                        String geomWKT = geometry.convertToWKT();
                                        sb.append(geomWKT).append("\n");
                                    }
                                    fastIterator.dispose();
                                }
                            }
                        }
                        getWktFromLayerArea.setText(sb.toString());
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });
        putWktToLayerButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                String text = putWktToLayerArea.getText().trim();
                if (text.length() != 0) {
                    GeometryManager geometryManager = GeometryLocator.getGeometryManager();
                    MapContext mapcontext = ProjectUtilities.getCurrentMapcontext();
                    try {
                        if (mapcontext != null) {
                            Geometry fromWKTGeom = geometryManager.createFrom(text);

                            FLyrVect selectedLayer = null;
                            FLayers layers = mapcontext.getLayers();
                            int layersCount = layers.getLayersCount();
                            for( int i = 0; i < layersCount; i++ ) {
                                FLayer layer = layers.getLayer(i);
                                if (layer instanceof FLyrVect) {
                                    FLyrVect vectorLayer = (FLyrVect) layer;
                                    FLayerStatus fLayerStatus = vectorLayer.getFLayerStatus();
                                    if (fLayerStatus.active && fLayerStatus.visible) {
                                        selectedLayer = vectorLayer;
                                        break;
                                    }
                                }
                            }

                            if (selectedLayer != null) {
                                FeatureStore featureStore = null;
                                // selectedLayer.setEditing(true);
                                try {
                                    featureStore = selectedLayer.getFeatureStore();
                                    if (featureStore.allowWrite()) {
                                        featureStore.edit();
                                        EditableFeature newFeature = featureStore.createNewFeature(true);
                                        newFeature.setDefaultGeometry(fromWKTGeom);
                                        featureStore.insert(newFeature);
                                        featureStore.finishEditing();
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    if (featureStore != null)
                                        featureStore.cancelEditing();
                                }
                            } else {
                                dialogManager.messageDialog("A layer needs to be selected and visible.", "WARNING",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {

                }
            }
        });
    }

    public JComponent asJComponent() {
        return this;
    }

}
