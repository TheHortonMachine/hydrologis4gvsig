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
package org.jgrasstools.gvsig.epanet.core;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorialUniqueValueLegend;
import org.gvsig.fmap.mapcontext.rendering.symbols.ISymbol;
import org.gvsig.fmap.mapcontext.rendering.symbols.SymbolManager;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.impl.VectorialUniqueValueLegend;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.line.ISimpleLineSymbol;
import org.gvsig.tools.dispose.DisposableIterator;
import org.jgrasstools.gvsig.epanet.core.style.ColorArrayInterpolator;
import org.jgrasstools.gvsig.epanet.database.ILinkResults;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.ResultsLinkParameters;

/**
 * Class to help results styling.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class EpanetResultsStyler {
    private static final String ID = "id";
    private static MapContextManager mapContextManager = MapContextLocator.getMapContextManager();

    public static VectorialUniqueValueLegend createPipesLegend( FLyrVect layer, List<ILinkResults> linksResults, float[] minMax,
            ResultsLinkParameters linkVar ) throws Exception {
        SymbolManager symbolManager = mapContextManager.getSymbolManager();
        VectorialUniqueValueLegend leg = (VectorialUniqueValueLegend) mapContextManager
                .createLegend(IVectorialUniqueValueLegend.LEGEND_NAME);

        float delta = (minMax[1] - minMax[0]) / 5;
        double[] ramp = new double[6];
        for( int i = 0; i < ramp.length; i++ ) {
            ramp[i] = minMax[0] + i * delta;
        }
        ColorArrayInterpolator colorInterpolator = new ColorArrayInterpolator(ramp, EpanetUtilities.rainbow);

        // FeatureStore elRs = layer.getFeatureStore();

        leg.setClassifyingFieldNames(new String[]{ID});
        leg.setShapeType(layer.getShapeType());

//        leg.getDefaultSymbol().setDescription("Default");
//        leg.addSymbol(null, leg.getDefaultSymbol());

        // TODO CHECK
        Color[] colorScheme = new Color[EpanetUtilities.rainbow.length];
        for( int i = 0; i < colorScheme.length; i++ ) {
            int[] rgb = EpanetUtilities.rainbow[i];
            colorScheme[i] = new Color(rgb[0], rgb[1], rgb[2]);
        }
        leg.setColorScheme(colorScheme);

        for( ILinkResults linkResult : linksResults ) {
            double value = -9999;
            switch( linkVar ) {
            case FLOW:
                value = linkResult.getFlow1();
                break;
            case VELOCITY:
                value = linkResult.getVelocity1();
                break;
            case ENERGY:
                value = linkResult.getEnergy();
                break;
            case HEADLOSS:
                value = linkResult.getHeadloss();
                break;
            case STATUS:
                value = linkResult.getStatus();
                break;
            default:
                throw new RuntimeException("Undefined variable.");
            }
            String id = linkResult.getId();
            int[] rgb = colorInterpolator.interpolate(value);
            Color color = new Color(rgb[0], rgb[1], rgb[2]);
            ISymbol theSymbol = symbolManager.createSymbol(layer.getShapeType(), color);
            if (theSymbol instanceof ISimpleLineSymbol) {
                ISimpleLineSymbol lineSymbol = (ISimpleLineSymbol) theSymbol;
                lineSymbol.setLineWidth(3);
            }
            theSymbol.setDescription(id + ": " + value);
            leg.addSymbol(id, theSymbol);
        }

        // FeatureSet set = null;
        // DisposableIterator iterator = null;
        // try {
        // set = elRs.getFeatureSet();
        //
        // iterator = set.fastIterator();
        // while( iterator.hasNext() ) {
        // Feature feature = (Feature) iterator.next();
        // Object pipeIdObject = feature.get(ID);
        // if (pipeIdObject == null) {
        // continue;
        // }
        // String pipeId = (String) pipeIdObject;
        // Double valueObj = valuesMap.get(pipeId);
        // if (valueObj != null) {
        // double value = valueObj;
        // int[] rgb = colorInterpolator.interpolate(value);
        // Color color = new Color(rgb[0], rgb[1], rgb[2]);
        // ISymbol theSymbol = symbolManager.createSymbol(layer.getShapeType(), color);
        // if (theSymbol instanceof ISimpleLineSymbol) {
        // ISimpleLineSymbol lineSymbol = (ISimpleLineSymbol) theSymbol;
        // lineSymbol.setLineWidth(3);
        // }
        // theSymbol.setDescription(pipeIdObject.toString() + ": " + value);
        // leg.addSymbol(pipeIdObject, theSymbol);
        // }
        // }
        // } finally {
        // if (iterator != null) {
        // iterator.dispose();
        // }
        // if (set != null) {
        // set.dispose();
        // }
        // }

        // Object[] values = auxLegend.getValues();
        // String[] descriptions = new String[values.length];
        // ISymbol[] symbols = new ISymbol[values.length];
        //
        // for( int i = 0; i < values.length; i++ ) {
        // Object value = values[i];
        // symbols[i] = auxLegend.getSymbolByValue(value);
        // descriptions[i] = symbols[i].getDescription();
        // }
        //
        // symbolTable.fillTableFromSymbolList(symbols, values, descriptions);

        return leg;
    }

}
