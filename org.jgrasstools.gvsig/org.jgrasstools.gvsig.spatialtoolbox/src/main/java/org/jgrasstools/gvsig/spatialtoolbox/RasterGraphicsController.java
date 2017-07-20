package org.jgrasstools.gvsig.spatialtoolbox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.fmap.dal.coverage.dataset.Buffer;
import org.gvsig.fmap.dal.coverage.datastruct.Extent;
import org.gvsig.fmap.dal.coverage.store.RasterDataStore;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.geom.primitive.Point;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.GraphicLayer;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.symbology.SymbologyLocator;
import org.gvsig.symbology.SymbologyManager;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.fill.ISimpleFillSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.line.ISimpleLineSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.marker.ISimpleMarkerSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.text.ISimpleTextSymbol;
import org.gvsig.tools.dynobject.DynObject;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.utils.colors.ColorInterpolator;
import org.jgrasstools.gears.utils.colors.EColorTables;
import org.jgrasstools.gui.utils.GuiUtilities;
import org.jgrasstools.gvsig.base.DefaultGvsigTables;
import org.jgrasstools.gvsig.base.GeometryUtilities;
import org.jgrasstools.gvsig.base.JGrasstoolsExtension;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.RasterUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class RasterGraphicsController extends RasterGraphicsView implements Component {
    private static final Logger logger = LoggerFactory.getLogger(RasterGraphicsController.class);
    private static final String RASTER_GRAPHICS_KEY = "RASTER_GRAPHICS_KEY";

    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String SHOWCOLROW = "showcolrow";
    private static final String SHOWSTEEPEST = "showsteepest";
    private static final String SHOWNUMBERS = "shownumbers";
    private static final String NUMFORMAT = "numformat";
    private static final String DEFAULT_NUMFORMAT = "0.00000";

    private MapContext currentMapcontext;
    private DynObject preferences;
    private HashMap<String, String> prefsMap = new HashMap<>();
    private ThreadSafeDialogsManager dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
    private HashMap<String, FLyrRaster> rasterLayerMap;
    private SymbologyManager symbologyManager;

    public RasterGraphicsController() {
        setPreferredSize(new Dimension(600, 300));

        preferences = ProjectUtilities.getPluginPreferences(JGrasstoolsExtension.class);
        Object prefsMapTmp = preferences.getDynValue(RASTER_GRAPHICS_KEY);
        if (prefsMapTmp != null) {
            prefsMap = (HashMap) prefsMapTmp;
        }
        init();
    }

    private void init() {
        currentMapcontext = ProjectUtilities.getCurrentMapcontext();
        symbologyManager = SymbologyLocator.getSymbologyManager();

        Object selectedRasterName = null;
        List<FLayer> selectedLayers = LayerUtilities.getSelectedLayers(currentMapcontext);
        if (selectedLayers.size() > 0) {
            FLayer selectedLayer = selectedLayers.get(0);
            if (selectedLayer instanceof FLyrRaster) {
                selectedRasterName = selectedLayer.getName();
            }
        }

        setCombos(selectedRasterName);

        String showNumbersStr = prefsMap.getOrDefault(SHOWNUMBERS, TRUE);
        _showNumbersCheck.setSelected(Boolean.parseBoolean(showNumbersStr));

        String showRowColsStr = prefsMap.getOrDefault(SHOWCOLROW, FALSE);
        _showRowColsCheck.setSelected(Boolean.parseBoolean(showRowColsStr));

        String showSteepestStr = prefsMap.getOrDefault(SHOWSTEEPEST, FALSE);
        _showSteepestDirectionCheck.setSelected(Boolean.parseBoolean(showSteepestStr));

        String numFormatStr = prefsMap.getOrDefault(NUMFORMAT, DEFAULT_NUMFORMAT);
        _numFormatField.setText(numFormatStr);

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

        _refreshButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                refreshGraphics();
            }
        });

        _clearButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                GraphicLayer graphicsLayer = currentMapcontext.getGraphicsLayer();
                graphicsLayer.clearAllGraphics();
                graphicsLayer.clearAllSymbols();
                currentMapcontext.invalidate();
            }
        });

    }

    protected void refreshGraphics() {
        if (currentMapcontext != null) {
            String layer = _rasterLayerCombo.getSelectedItem().toString();
            FLyrRaster selectedRaster = rasterLayerMap.get(layer);

            Envelope envelope = currentMapcontext.getViewPort().getEnvelope();
            Point ll = envelope.getLowerCorner();
            Point ur = envelope.getUpperCorner();

            double w = ll.getX();
            double s = ll.getY();
            double e = ur.getX();
            double n = ur.getY();

            // Extent ext = new ExtentImpl(w, n, e, s);
            try {
                boolean showNumbers = _showNumbersCheck.isSelected();
                boolean showSteep = _showSteepestDirectionCheck.isSelected();
                boolean showColrRow = _showRowColsCheck.isSelected();

                RasterDataStore dataStore = selectedRaster.getDataStore();
                Extent extent = dataStore.getExtent();
                double cellSize = dataStore.getCellSize();
                int cols = (int) dataStore.getWidth();
                int rows = (int) dataStore.getHeight();

                Point2D llP = new Point2D.Double(w, s);
                Point2D urP = new Point2D.Double(e, n);

                Point2D llPix = dataStore.worldToRaster(llP);
                Point2D urPix = dataStore.worldToRaster(urP);

                int fromC = (int) Math.floor(llPix.getX());
                int toC = (int) Math.ceil(urPix.getX());
                int fromR = (int) Math.floor(urPix.getY());
                int toR = (int) Math.ceil(llPix.getY());

                int cellCount = (toC - fromC) * (toR - fromR);
                if (cellCount > 1E5) {
                    ApplicationManager applicationManager = ApplicationLocator.getManager();
                    JComponent mainComponent = applicationManager.getActiveDocument().getMainComponent();
                    GuiUtilities.showWarningMessage(mainComponent, "WARNING",
                            "This command supports only up to 100000 cells. The current view contains: " + cellCount);
                    return;
                }

                DecimalFormat f;
                try {
                    f = new DecimalFormat(_numFormatField.getText());
                } catch (Exception e2) {
                    // use default
                    f = new DecimalFormat(DEFAULT_NUMFORMAT);
                }
                Buffer buffer = RasterUtilities.readSingleBandRasterData(dataStore, extent, null);
                double[] minMax = getMinMax(buffer, fromC, toC, fromR, toR, cols, rows);

                ColorInterpolator colorInterpolator = new ColorInterpolator(EColorTables.elev.name(), minMax[0], minMax[1], 255);
                GraphicLayer graphicsLayer = currentMapcontext.getGraphicsLayer();
                graphicsLayer.clearAllGraphics();
                graphicsLayer.clearAllSymbols();

                int dataType = buffer.getDataType();
                for( int r = fromR; r < toR; r++ ) {
                    if (r < 0 || r >= rows)
                        continue;
                    for( int c = fromC; c < toC; c++ ) {
                        if (c < 0 || c >= cols)
                            continue;
                        double value = getValue(buffer, dataType, r, c);
                        if (JGTConstants.isNovalue(value)) {
                            continue;
                        }

                        Color interpColor = colorInterpolator.getColorFor(value);

                        Point2D pt = new Point2D.Double(c, r);
                        Point2D currentCellPosition = dataStore.rasterToWorld(pt);

                        Coordinate[] coords = new Coordinate[5];
                        coords[0] = new Coordinate(currentCellPosition.getX(), currentCellPosition.getY());
                        coords[1] = new Coordinate(currentCellPosition.getX(), currentCellPosition.getY() - cellSize);
                        coords[2] = new Coordinate(currentCellPosition.getX() + cellSize, currentCellPosition.getY() - cellSize);
                        coords[3] = new Coordinate(currentCellPosition.getX() + cellSize, currentCellPosition.getY());
                        coords[4] = coords[0];
                        Geometry polygon = GeometryUtilities.createPolygon2D(coords);

                        ISimpleFillSymbol symbol = getSymbol(interpColor);
                        int symbolId = graphicsLayer.addSymbol(symbol);
                        graphicsLayer.addGraphic("g", polygon, symbolId, null, "cells", 0);

                        double half = cellSize / 2;
                        if (showNumbers) {
                            ISimpleTextSymbol valueSymbol = symbologyManager.createSimpleTextSymbol();
                            valueSymbol.setColor(Color.BLACK);
                            valueSymbol.setDrawWithHalo(true);
                            valueSymbol.setHaloColor(Color.WHITE);

                            if (showColrRow) {
                                valueSymbol.setText(c + " / " + r);
                            } else {
                                valueSymbol.setText(f.format(value));
                            }
                            int textSymbolId = graphicsLayer.addSymbol(valueSymbol);

                            double delta = cellSize * 0.1;
                            Geometry point = GeometryUtilities.createPoint2D(currentCellPosition.getX() + delta,
                                    currentCellPosition.getY() - half);
                            graphicsLayer.addGraphic("text", point, textSymbolId, null, "texts", 2);
                        }

                    }
                }
                for( int r = fromR; r < toR; r++ ) {
                    if (r < 0 || r >= rows)
                        continue;
                    for( int c = fromC; c < toC; c++ ) {
                        if (c < 0 || c >= cols)
                            continue;
                        double value = getValue(buffer, dataType, r, c);
                        if (JGTConstants.isNovalue(value)) {
                            continue;
                        }

                        Point2D pt = new Point2D.Double(c, r);
                        Point2D currentCellPosition = dataStore.rasterToWorld(pt);

                        double half = cellSize / 2;

                        if (showSteep) {
                            int steepestCol = c;
                            int steepestRow = r;
                            double minValue = value;
                            boolean doBreak = false;
                            for( int tmpR = -1; tmpR <= 1; tmpR++ ) {
                                int row = r + tmpR;
                                if (row < 0 || row >= rows)
                                    continue;
                                for( int tmpC = -1; tmpC <= 1; tmpC++ ) {
                                    if (tmpR == 0 && tmpC == 0) {
                                        continue;
                                    }

                                    int col = c + tmpC;
                                    if (col < 0 || col >= cols)
                                        continue;
                                    double v = getValue(buffer, dataType, row, col);
                                    if (JGTConstants.isNovalue(v)) {
                                        continue;
                                    }

                                    if (v < minValue) {
                                        minValue = v;
                                        steepestCol = col;
                                        steepestRow = row;
                                    }
                                }
                                if (doBreak) {
                                    break;
                                }
                            }
                            if (doBreak) {
                                continue;
                            }
                            Point2D pt2 = new Point2D.Double(steepestCol, steepestRow);
                            Point2D nextCellPosition = dataStore.rasterToWorld(pt2);

                            LineSegment ls = new LineSegment(
                                    new Coordinate(currentCellPosition.getX() + half, currentCellPosition.getY() - half),
                                    new Coordinate(nextCellPosition.getX() + half, nextCellPosition.getY() - half));
                            Coordinate c1 = ls.pointAlong(0.1);
                            Coordinate c2 = ls.pointAlong(0.9);

                            if (ls.getLength() > cellSize * 2) {
                                System.out.println();
                            }

                            Coordinate[] lineCoords = {c1, c2};

                            Geometry line = GeometryUtilities.createLine2D(lineCoords);
                            Geometry point = GeometryUtilities.createPoint2D(lineCoords[0].x, lineCoords[0].y);

                            // create symbols and graphics
                            Color color = Color.BLUE;
                            if (steepestCol == c && steepestRow == r) {
                                color = Color.RED;
                            }
                            ISimpleLineSymbol lineSymbol = symbologyManager.createSimpleLineSymbol();
                            lineSymbol.setColor(color);
                            lineSymbol.setLineWidth(1);
                            int lineSymbolId = graphicsLayer.addSymbol(lineSymbol);

                            ISimpleMarkerSymbol pointSymbol = symbologyManager.createSimpleMarkerSymbol();
                            pointSymbol.setColor(color);
                            pointSymbol.setSize(4);
                            int pointSymbolId = graphicsLayer.addSymbol(pointSymbol);

                            graphicsLayer.addGraphic("line", line, lineSymbolId, null, "lines", 1);
                            graphicsLayer.addGraphic("line", point, pointSymbolId, null, "lines", 1);
                        }

                    }
                }
                currentMapcontext.invalidate();
                // ApplicationManager applicationManager = ApplicationLocator.getManager();
                // JComponent mainComponent =
                // applicationManager.getActiveDocument().getMainComponent();
                // GuiUtilities.showInfoMessage(mainComponent, "INFO",
                // "Current selected raster values copied to clipboard. Cells: " + cellCount);
            } catch (Exception e1) {
                logger.error("Error", e1);
            }

        }
    }

    private double getValue( Buffer buffer, int dataType, int r, int c ) {
        double value;
        if (dataType == Buffer.TYPE_BYTE) {
            value = buffer.getElemByte(r, c, 0);
        } else if ((dataType == Buffer.TYPE_SHORT) | (dataType == Buffer.TYPE_USHORT)) {
            value = buffer.getElemShort(r, c, 0);
        } else if (dataType == Buffer.TYPE_INT) {
            value = buffer.getElemInt(r, c, 0);
        } else if (dataType == Buffer.TYPE_FLOAT) {
            value = buffer.getElemFloat(r, c, 0);
        } else if (dataType == Buffer.TYPE_DOUBLE) {
            value = buffer.getElemDouble(r, c, 0);
        } else {
            value = JGTConstants.doubleNovalue;
        }
        return value;
    }

    private ISimpleFillSymbol getSymbol( Color col ) {
        ISimpleLineSymbol outlineSymbol = symbologyManager.createSimpleLineSymbol();
        outlineSymbol.setLineWidth(1);
        outlineSymbol.setLineColor(Color.BLACK);

        ISimpleFillSymbol simpleFillSymbol = symbologyManager.createSimpleFillSymbol();
        simpleFillSymbol.setOutline(outlineSymbol);
        simpleFillSymbol.setFillColor(col);
        return simpleFillSymbol;
    }

    private double[] getMinMax( Buffer buffer, int fromC, int toC, int fromR, int toR, int cols, int rows ) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        int dataType = buffer.getDataType();
        for( int r = fromR; r < toR; r++ ) {
            if (r < 0 || r >= rows)
                continue;
            for( int c = fromC; c < toC; c++ ) {
                if (c < 0 || c >= cols)
                    continue;
                double value = 0;
                if (dataType == Buffer.TYPE_BYTE) {
                    value = buffer.getElemByte(r, c, 0);
                } else if ((dataType == Buffer.TYPE_SHORT) | (dataType == Buffer.TYPE_USHORT)) {
                    value = buffer.getElemShort(r, c, 0);
                } else if (dataType == Buffer.TYPE_INT) {
                    value = buffer.getElemInt(r, c, 0);
                } else if (dataType == Buffer.TYPE_FLOAT) {
                    value = buffer.getElemFloat(r, c, 0);
                } else if (dataType == Buffer.TYPE_DOUBLE) {
                    try {
                        value = buffer.getElemDouble(r, c, 0);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    value = JGTConstants.doubleNovalue;
                }

                if (!JGTConstants.isNovalue(value)) {
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
            }
        }
        return new double[]{min, max};
    }

    protected void freeResources() {
        boolean selected = _showNumbersCheck.isSelected();
        prefsMap.put(SHOWNUMBERS, selected ? TRUE : FALSE);
        selected = _showSteepestDirectionCheck.isSelected();
        prefsMap.put(SHOWSTEEPEST, selected ? TRUE : FALSE);
        selected = _showRowColsCheck.isSelected();
        prefsMap.put(SHOWCOLROW, selected ? TRUE : FALSE);
        prefsMap.put(NUMFORMAT, _numFormatField.getText());
        preferences.setDynValue(RASTER_GRAPHICS_KEY, prefsMap);
    }

    private String[] getRasterLayers() {
        rasterLayerMap = new HashMap<String, FLyrRaster>();

        List<FLyrRaster> rasterLayers = LayerUtilities.getRasterLayers(currentMapcontext);
        String[] rasterNames = new String[rasterLayers.size()];
        for( int i = 0; i < rasterNames.length; i++ ) {
            FLyrRaster fLyrRaster = rasterLayers.get(i);
            rasterNames[i] = fLyrRaster.getName();
            rasterLayerMap.put(rasterNames[i], fLyrRaster);
        }
        return rasterNames;
    }

    private void setCombos( Object selectedRaster ) {
        String[] rasterLayers = getRasterLayers();

        _rasterLayerCombo.setModel(new DefaultComboBoxModel<String>(rasterLayers));

        if (selectedRaster != null) {
            _rasterLayerCombo.setSelectedItem(selectedRaster);
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
        setCombos(null);
    }

}
