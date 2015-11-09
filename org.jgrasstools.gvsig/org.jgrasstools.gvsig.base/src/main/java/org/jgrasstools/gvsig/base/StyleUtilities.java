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

import org.gvsig.andami.plugins.IExtension;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorLegend;
import org.gvsig.fmap.mapcontext.rendering.legend.styling.ILabelingStrategy;
import org.gvsig.symbology.SymbologyLocator;
import org.gvsig.symbology.SymbologyManager;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.impl.SingleSymbolLegend;
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

    public static void createSingleSymbolLegend( String legendName ) {
        SingleSymbolLegend leg = (SingleSymbolLegend) mapContextManager.createLegend(legendName);
        // SingleSymbolLegend leg = (SingleSymbolLegend)
        // mapContextManager.createLegend(name);
        //
        //
        // SymbolManager symbolManager = MapContextLocator.getSymbolManager();
        // sym = symbolManager.createSymbol(name);
        // sym.SetColor(..)
        // leg.setDefaultSymbol(sym);
        //
        // blayer.setLegend(null);
        // blayer.setLabelingStrategy(null);
        //
        // ILabelingStrategy labeling;
        //
        // PersistenceManager pm = ToolsLocator.getPersistenceManager();
        // pm.saveState(pm.getState(labeling), new FileOutputStream(file));
        //
        // labeling = (ILabelingStrategy) pm.loadState(null);
    }

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

}
