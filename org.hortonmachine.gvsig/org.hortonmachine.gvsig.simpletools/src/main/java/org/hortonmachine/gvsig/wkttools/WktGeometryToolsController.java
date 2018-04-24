package org.hortonmachine.gvsig.wkttools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.gvsig.andami.IconThemeHelper;
import org.gvsig.app.gui.panels.CRSSelectPanelFactory;
import org.gvsig.app.gui.panels.crs.ISelectCrsPanel;
import org.gvsig.crs.Crs;
import org.gvsig.crs.CrsException;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.geom.primitive.Point;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayerStatus;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.layers.vectorial.GraphicLayer;
import org.gvsig.fmap.mapcontext.rendering.symbols.ISymbol;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.hortonmachine.gvsig.base.CrsUtilities;
import org.hortonmachine.gvsig.base.GtGvsigConversionUtilities;
import org.hortonmachine.gvsig.base.HMUtilities;
import org.hortonmachine.gvsig.base.ProjectUtilities;
import org.hortonmachine.gvsig.base.StyleUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.io.WKTReader;

/**
 * WKT Geometry tool gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class WktGeometryToolsController extends WktGeometryToolsView implements Component {
    private static final double DEFAULT_ZOOM_BUFFER = 10;
    private ThreadSafeDialogsManager dialogManager;

    public WktGeometryToolsController() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(600, 400));
        MapContext mapcontext = ProjectUtilities.getCurrentMapcontext();

        clearGlassPane();
        _zoomToCheckbox.setSelected(true);
        _zoomBufferField.setText("" + DEFAULT_ZOOM_BUFFER);

        _getWktFromLayerArea.setLineWrap(true);
        _putWktToLayerArea.setLineWrap(true);

        _drawOnGlassPane.setSelected(true);
        
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        ImageIcon copyIcon = IconThemeHelper.getImageIcon("copy");
        _copyWktButton.setIcon(copyIcon);
        _copyWktButton.addActionListener(e -> {
            String wktText = _getWktFromLayerArea.getText();
            if (wktText.trim().length() != 0) {
                String crsText = _crsTextField.getText();
                try {
                    CoordinateReferenceSystem crs = org.hortonmachine.gears.utils.CrsUtilities.getCrsFromEpsg(crsText);

                    IProjection mapProjection = mapcontext.getProjection();
                    CoordinateReferenceSystem mapCrs = GtGvsigConversionUtilities.gvsigCrs2gtCrs(mapProjection);

                    WKTReader reader = new WKTReader();
                    com.vividsolutions.jts.geom.Geometry jtsGeometry = reader.read(wktText);
                    MathTransform transform = CRS.findMathTransform(mapCrs, crs);
                    com.vividsolutions.jts.geom.Geometry targetGeometry = JTS.transform(jtsGeometry, transform);

                    wktText = targetGeometry.toText();

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                HMUtilities.copyToClipboard(wktText);
            }
        });

        _selectCrsButton.addActionListener(e -> {
            ISelectCrsPanel csSelect = CRSSelectPanelFactory.getUIFactory().getSelectCrsPanel(null, true);
            ToolsSwingLocator.getWindowManager().showWindow((JComponent) csSelect,
                    "Please insert the CRS EPSG code for the required projection.", MODE.DIALOG);
            if (csSelect.isOkPressed() && csSelect.getProjection() != null) {
                IProjection selectedProjection = csSelect.getProjection();
                try {
                    String epsg = selectedProjection.getAbrev();
                    // CoordinateReferenceSystem crs =
                    // GtGvsigConversionUtilities.gvsigCrs2gtCrs(selectedProjection);
                    // String crsWkt = crs.toWKT();
                    _crsTextField.setText(epsg);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });

        _getWktFromLayerButton.addActionListener(e -> {
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
                    _getWktFromLayerArea.setText(sb.toString());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        _clearGlasspaneButton.addActionListener(e -> {
            clearGlassPane();
        });

        _putWktToLayerButton.addActionListener(e -> {
            String text = _putWktToLayerArea.getText().trim();
            if (text.length() != 0) {
                GeometryManager geometryManager = GeometryLocator.getGeometryManager();
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

                        if (_drawOnGlassPane.isSelected()) {
                            if (_geomIsLatLongCheck.isSelected()) {
                                // convert to current mapview
                                IProjection mapCrs = mapcontext.getViewPort().getProjection();
                                reprojectFromLatLongToCrs(mapCrs, fromWKTGeom);
                            }

                            GraphicLayer graphicsLayer = mapcontext.getGraphicsLayer();

                            ISymbol symbol = new StyleUtilities.SymbolBuilder().strokeColor(Color.WHITE).fillColor(Color.YELLOW)
                                    .size(20).build(fromWKTGeom.getType());
                            int symbolId = graphicsLayer.addSymbol(symbol);
                            graphicsLayer.addGraphic("g", fromWKTGeom, symbolId, null, "fromWKTGeom", 0);
                            mapcontext.invalidate();
                        } else if (selectedLayer != null) {
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

                                if (_geomIsLatLongCheck.isSelected()) {
                                    // convert to current layer
                                    IProjection layerCrs = featureStore.getDefaultFeatureType().getDefaultSRS();
                                    reprojectFromLatLongToCrs(layerCrs, fromWKTGeom);
                                }

                                if (_zoomToCheckbox.isSelected()) {
                                    String zoomBufferStr = _zoomBufferField.getText();
                                    double zoomBuffer = DEFAULT_ZOOM_BUFFER;
                                    try {
                                        zoomBuffer = Double.parseDouble(zoomBufferStr);
                                    } catch (Exception e1) {
                                        // ignore and use default
                                    }
                                    Envelope env = fromWKTGeom.getEnvelope();
                                    Point ll = env.getLowerCorner();
                                    Point ur = env.getUpperCorner();
                                    Envelope zoomEnvelope = GeometryLocator.getGeometryManager().createEnvelope(
                                            ll.getX() - zoomBuffer, ll.getY() - zoomBuffer, ur.getX() + zoomBuffer,
                                            ur.getY() + zoomBuffer, Geometry.SUBTYPES.GEOM2D);

                                    Envelope reprojectedEnvelope = CrsUtilities.reprojectToMapCrs(zoomEnvelope,
                                            selectedLayer.getProjection(), mapcontext);
                                    mapcontext.getViewPort().setEnvelope(reprojectedEnvelope);
                                    mapcontext.invalidate();
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

        });

        addComponentListener(new ComponentListener(){

            public void componentShown( ComponentEvent e ) {
            }

            public void componentResized( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentHidden( ComponentEvent e ) {
                MapContext mapcontext = ProjectUtilities.getCurrentMapcontext();
                GraphicLayer graphicsLayer = mapcontext.getGraphicsLayer();
                graphicsLayer.clearAllGraphics();
                graphicsLayer.clearAllSymbols();
            }
        });
    }

    private void clearGlassPane() {
        MapContext mapcontext = ProjectUtilities.getCurrentMapcontext();
        if (mapcontext != null) {
            GraphicLayer graphicsLayer = mapcontext.getGraphicsLayer();
            graphicsLayer.clearAllGraphics();
            graphicsLayer.clearAllSymbols();
            mapcontext.invalidate();
        }
    }

    private void reprojectFromLatLongToCrs( IProjection toCrs, Geometry fromWKTGeom ) throws CrsException {
        Crs fromCrs = new Crs("EPSG:4326");
        ICoordTrans coordTrans = fromCrs.getCT(toCrs);
        fromWKTGeom.reProject(coordTrans);
    }

    public JComponent asJComponent() {
        return this;
    }

}
