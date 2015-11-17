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
package org.jgrasstools.gvsig.base;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.andami.plugins.IExtension;
import org.gvsig.fmap.dal.coverage.datastruct.ColorItem;
import org.gvsig.fmap.dal.coverage.store.props.ColorTable;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorLegend;
import org.gvsig.fmap.mapcontext.rendering.legend.styling.ILabelingStrategy;
import org.gvsig.fmap.mapcontext.rendering.symbols.ISymbol;
import org.gvsig.raster.fmap.legend.ColorTableLegend;
import org.gvsig.raster.impl.datastruct.ColorItemImpl;
import org.gvsig.raster.impl.store.properties.DataStoreColorTable;
import org.gvsig.symbology.SymbologyLocator;
import org.gvsig.symbology.SymbologyManager;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.impl.SingleSymbolLegend;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.fill.IFillSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.line.ILineSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.line.ISimpleLineSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.marker.IMarkerSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.marker.IPictureMarkerSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.marker.ISimpleMarkerSymbol;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.persistence.PersistenceManager;

/**
 * Style utilities.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StyleUtilities {
    public static final String SINGLE_SYMBOL_LEGEND = "SingleSymbol";

    private static MapContextManager mapContextManager = MapContextLocator.getMapContextManager();

    /**
     * Create a simple point type legend defining some properties.
     * 
     * @param symbolType the stype, as off: {@link IMarkerSymbol#CIRCLE_STYLE}, ...
     * @param size the symbol size.
     * @param fillColor the symbol's fill color.
     * @param fillTransparency the symbol's fill transparency color.
     * @param strokeColor the symbol's stroke color.
     * @param strokeWidth the symbol's stroke width.
     * @return the created vector legend.
     */
    public static IVectorLegend createSimplePointLegend( int symbolType, double size, Color fillColor, int fillTransparency,
            Color strokeColor, double strokeWidth ) {
        SingleSymbolLegend leg = (SingleSymbolLegend) mapContextManager.createLegend(SINGLE_SYMBOL_LEGEND);

        SymbologyManager symbologyManager = SymbologyLocator.getSymbologyManager();
        ISimpleMarkerSymbol simpleMarkerSymbol = symbologyManager.createSimpleMarkerSymbol();

        simpleMarkerSymbol.setSize(size);
        simpleMarkerSymbol.setColor(fillColor);
        simpleMarkerSymbol.setAlpha(fillTransparency);
        simpleMarkerSymbol.setOutlined(strokeColor != null);
        if (strokeColor != null) {
            simpleMarkerSymbol.setOutlineColor(strokeColor);
            simpleMarkerSymbol.setOutlineSize(strokeWidth);
        }
        simpleMarkerSymbol.setStyle(symbolType);

        leg.setDefaultSymbol(simpleMarkerSymbol);

        return leg;
    }

    /**
     * Create a simple legend based on an image.
     * 
     * @param pluginClass the plugin in which to find the image.
     * @param imagePath the relative path to the image. If pluginClass is <code>null</code>, 
     *                  this path will be handled as absolute. 
     * @param selectedImagePath the selected image to use.
     * @param size the size of the marker.
     * @return the legend.
     * @throws IOException
     */
    public static IVectorLegend createImagePointLegend( Class< ? extends IExtension> pluginClass, String imagePath,
            String selectedImagePath, double size ) throws IOException {
        File imageFile;
        File selImageFile;
        if (pluginClass != null) {
            imageFile = ProjectUtilities.getFileInPlugin(pluginClass, imagePath);
            selImageFile = ProjectUtilities.getFileInPlugin(pluginClass, selectedImagePath);
        } else {
            imageFile = new File(imagePath);
            selImageFile = new File(selectedImagePath);
        }
        URL imageUrl = null;
        if (imageFile.exists()) {
            imageUrl = imageFile.toURI().toURL();
        }
        URL selImageUrl = null;
        if (selImageFile.exists()) {
            selImageUrl = selImageFile.toURI().toURL();
        }

        SingleSymbolLegend leg = (SingleSymbolLegend) mapContextManager.createLegend(SINGLE_SYMBOL_LEGEND);

        SymbologyManager symbologyManager = SymbologyLocator.getSymbologyManager();
        IPictureMarkerSymbol pictureMarkerSymbol = symbologyManager.createPictureMarkerSymbol(imageUrl, selImageUrl);

        pictureMarkerSymbol.setSize(size);

        leg.setDefaultSymbol(pictureMarkerSymbol);
        return leg;
    }

    /**
     * Create a simple line type legend defining some properties.
     * 
     * @param strokeColor the symbol's stroke color.
     * @param strokeWidth the symbol's stroke width.
     * @param strokeTransparency the symbol's stroke alpha.
     * @return the created vector legend.
     */
    public static IVectorLegend createSimpleLineLegend( Color strokeColor, double strokeWidth, int strokeTransparency ) {
        SingleSymbolLegend leg = (SingleSymbolLegend) mapContextManager.createLegend(SINGLE_SYMBOL_LEGEND);

        SymbologyManager symbologyManager = SymbologyLocator.getSymbologyManager();
        ISimpleLineSymbol simpleLineSymbol = symbologyManager.createSimpleLineSymbol();

        simpleLineSymbol.setLineWidth(strokeWidth);
        simpleLineSymbol.setLineColor(strokeColor);
        simpleLineSymbol.setAlpha(strokeTransparency);

        leg.setDefaultSymbol(simpleLineSymbol);
        return leg;
    }

    /**
     * Get the labeling strategy from file.
     * 
     * @param gvslabFile the labeling file (previously saved through {@link #saveLabelsToFile(FLyrVect, File)}).
     * @return the read labeling strategy.
     * @throws FileNotFoundException
     */
    public static ILabelingStrategy getLabelsFromFile( File gvslabFile ) throws FileNotFoundException {
        if (!gvslabFile.exists()) {
            return null;
        }
        PersistenceManager persistenceManager = ToolsLocator.getPersistenceManager();
        InputStream labStream = new FileInputStream(gvslabFile);
        Object labeling = persistenceManager.getObject(labStream);
        if (labeling instanceof ILabelingStrategy) {
            ILabelingStrategy labelingStrategy = (ILabelingStrategy) labeling;
            return labelingStrategy;
        }
        return null;
    }

    /**
     * Save a labeling strategy to file.
     * 
     * @param vectorLayer the layer to get the labeling from.
     * @param outFile the file to which to dump to.
     * @throws IOException
     */
    public static void saveLabelsToFile( FLyrVect vectorLayer, File outFile ) throws IOException {
        ILabelingStrategy labelingStrategy = vectorLayer.getLabelingStrategy();

        PersistenceManager persistenceManager = ToolsLocator.getPersistenceManager();
        persistenceManager.putObject(new FileOutputStream(outFile), labelingStrategy);
    }

    /**
     * Set the labeling strategy of a layer.
     * 
     * @param vectorLayer the layer to set labeling to.
     * @param labelingStrategy the labeling strategy to set.
     * @throws IOException
     */
    public static void setLabelsToLayer( FLyrVect vectorLayer, ILabelingStrategy labelingStrategy ) throws IOException {
        vectorLayer.setLabelingStrategy(labelingStrategy);
        vectorLayer.setIsLabeled(true);
    }

    // Read legend from file: MapContextManager
    //
    // /**
    // * Creates a legend writer for the specified legend class
    // *
    // */
    // ILegendWriter createLegendWriter(Class legendClass, String format)
    // throws MapContextException;
    //
    // /**
    // * Creates a legend reader for the given format
    // * ("sld", "gvsleg", etc are extracted from the MIME long string)
    // */
    // ILegendReader createLegendReader(String format)
    // throws MapContextRuntimeException;
    //
    // /**
    // *
    // * Format is a MIME type string. Examples:
    // *
    // * "application/zip; subtype=gvsleg",
    // * "text/xml; subtype=sld/1.0.0",
    // * "text/xml; subtype=sld/1.1.0",
    // *
    // * @return A list of Strings with the available formats for reading
    // * legends
    // */
    // List getLegendReadingFormats(); <- gives the supported formats

    /**
     * Create style for a given colortable.
     *
     *
     * @param colorTableName the name of the colortable (has to be available in {@link org.jgrasstools.gears.utils.colors.DefaultTables).
     * @param min
     * @param max
     * @param transparency between 0 - 255
     * @param numFormat 
     * @param interpolate 
     * @return the legend.
     * @throws Exception
     */
    public static RasterStyleWrapper createRasterLegend4Colortable( String colorTableName, double min, double max,
            int transparency, String numFormat, boolean interpolate ) throws Exception {
        if (numFormat == null) {
            numFormat = "#.00";
        }
        DecimalFormat formatter = new DecimalFormat(numFormat);
        List<ColorItem> colorItems = new ArrayList<ColorItem>();

        String tableString = new DefaultGvsigTables().getTableString(colorTableName);
        if (tableString == null) {
            return null;
        }
        String[] split = tableString.split("\n");
        List<String> acceptedLines = new ArrayList<String>();
        for( String lineStr : split ) {
            if (lineStr.startsWith("#")) { //$NON-NLS-1$
                continue;
            }
            acceptedLines.add(lineStr);
        }
        int rulesCount = acceptedLines.size();

        double[] interpolatedValues = new double[rulesCount];
        double delta = (max - min) / (rulesCount - 1);
        for( int i = 0; i < interpolatedValues.length; i++ ) {
            interpolatedValues[i] = min + i * delta;
        }

        ILineSymbol line = SymbologyLocator.getSymbologyManager().createSimpleLineSymbol();
        line.setLineColor(Color.BLACK);
        ISymbol[] symbol = new ISymbol[rulesCount];
        String[] desc = new String[rulesCount];

        for( int i = 0; i < acceptedLines.size(); i++ ) {
            String lineStr = acceptedLines.get(i);
            String[] lineSplit = lineStr.trim().split("\\s+"); //$NON-NLS-1$
            if (lineSplit.length == 3) {
                double interpolatedValue = interpolatedValues[i];
                String valueStr = formatter.format(interpolatedValue);
                int r = Integer.parseInt(lineSplit[0]);
                int g = Integer.parseInt(lineSplit[1]);
                int b = Integer.parseInt(lineSplit[2]);

                ColorItemImpl item = new ColorItemImpl();
                Color color = new Color(r, g, b, transparency);
                item.setColor(color);
                item.setValue(interpolatedValue);
                item.setNameClass(valueStr);
                colorItems.add(item);

                addRule(i, rulesCount, symbol, desc, line, color, valueStr, null);
            } else if (lineSplit.length == 4) {
                double v1 = Double.parseDouble(lineSplit[0]);
                int r1 = Integer.parseInt(lineSplit[1]);
                int g1 = Integer.parseInt(lineSplit[2]);
                int b1 = Integer.parseInt(lineSplit[3]);

                String valueStr = formatter.format(v1);

                ColorItemImpl item = new ColorItemImpl();
                Color color = new Color(r1, g1, b1, transparency);
                item.setColor(color);
                item.setValue(v1);
                item.setNameClass(valueStr);
                colorItems.add(item);
                addRule(i, rulesCount, symbol, desc, line, color, valueStr, null);
            }
        }

        RasterStyleWrapper wrapper = new RasterStyleWrapper();
        wrapper.legend = new ColorTableLegend(symbol, desc);
        ColorTable colorTable = new DataStoreColorTable(colorItems, false);
        colorTable.setInterpolated(interpolate);
        wrapper.colorTable = colorTable;
        return wrapper;
    }

    private static void addRule( int i, int count, ISymbol[] symbol, String[] desc, ILineSymbol line, Color color, String value,
            String label ) {
        IFillSymbol s = SymbologyLocator.getSymbologyManager().createSimpleFillSymbol();
        s.setOutline(line);
        s.setFillColor(color);
        if ((label == null) || (label.equals(""))) {
            desc[i] = "[" + value + "] ";
        } else {
            desc[i] = label;
        }
        symbol[i] = s;
    }

}
