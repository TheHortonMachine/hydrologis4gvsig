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

import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.impl.SingleSymbolLegend;

/**
 * Style utilities.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StyleUtilities {
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
    
//    Read legend from file: MapContextManager
//
//        /**
//         * Creates a legend writer for the specified legend class
//         * 
//         */
//        ILegendWriter createLegendWriter(Class legendClass, String format)
//                throws MapContextException;
//
//        /**
//         * Creates a legend reader for the given format
//         * ("sld", "gvsleg", etc are extracted from the MIME long string)
//         */
//        ILegendReader createLegendReader(String format)
//                throws MapContextRuntimeException;
//        
//        /**
//         * 
//         * Format is a MIME type string. Examples:
//         * 
//         * "application/zip; subtype=gvsleg",
//         * "text/xml; subtype=sld/1.0.0",
//         * "text/xml; subtype=sld/1.1.0",
//         * 
//         * @return A list of Strings with the available formats for reading
//         * legends
//         */
//        List getLegendReadingFormats(); <- gives the supported formats



}
