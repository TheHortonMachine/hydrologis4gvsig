/*
 * This file is part of HortonMachine (http://www.hortonmachine.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * The HortonMachine is free software: you can redistribute it and/or modify
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
package org.hortonmachine.gvsig.epanet.core;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

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
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.marker.ISimpleMarkerSymbol;
import org.gvsig.tools.dispose.DisposableIterator;
import org.hortonmachine.gvsig.epanet.core.style.ColorArrayInterpolator;
import org.hortonmachine.gvsig.epanet.database.ILinkResults;
import org.hortonmachine.gvsig.epanet.database.INodeResults;
import org.hortonmachine.hmachine.modules.networktools.epanet.core.ResultsLinkParameters;
import org.hortonmachine.hmachine.modules.networktools.epanet.core.ResultsNodeParameters;

/**
 * Class to help results styling.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class EpanetResultsStyler {
    private static final String ID = "id";
    private static MapContextManager mapContextManager = MapContextLocator.getMapContextManager();

    public static VectorialUniqueValueLegend createPipesLegend( FLyrVect layer, List<ILinkResults> linksResults,
            ResultsLinkParameters linkVar, float[] linksMinMax ) throws Exception {
        SymbolManager symbolManager = mapContextManager.getSymbolManager();
        VectorialUniqueValueLegend leg = (VectorialUniqueValueLegend) mapContextManager
                .createLegend(IVectorialUniqueValueLegend.LEGEND_NAME);

        leg.setClassifyingFieldNames(new String[]{ID});
        leg.setShapeType(layer.getShapeType());

        Color[] colorScheme = new Color[EpanetUtilities.rainbow.length];
        for( int i = 0; i < colorScheme.length; i++ ) {
            int[] rgb = EpanetUtilities.rainbow[i];
            colorScheme[i] = new Color(rgb[0], rgb[1], rgb[2]);
        }
        leg.setColorScheme(colorScheme);

        /*
         * first find min and max for the color ramp
         */
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;
        if (linksMinMax == null) {
            for( ILinkResults linkResult : linksResults ) {
                double value = getValue(linkVar, linkResult);
                min = (float) Math.min(value, min);
                max = (float) Math.max(value, max);
            }
        } else {
            min = linksMinMax[0];
            max = linksMinMax[1];
        }

        float delta = (max - min) / 5;
        double[] ramp = new double[6];
        for( int i = 0; i < ramp.length; i++ ) {
            ramp[i] = min + i * delta;
        }
        ColorArrayInterpolator colorInterpolator = new ColorArrayInterpolator(ramp, EpanetUtilities.rainbow);

        /*
         * then create color rules
         */
        DecimalFormat formatter = new DecimalFormat("0.00");
        TreeSet<String> idSet = new TreeSet<String>();
        for( ILinkResults linkResult : linksResults ) {
            double value = getValue(linkVar, linkResult);
            String id = linkResult.getId();
            idSet.add(id);
            int[] rgb = colorInterpolator.interpolate(value);
            Color color = new Color(rgb[0], rgb[1], rgb[2]);
            ISymbol theSymbol = symbolManager.createSymbol(layer.getShapeType(), color);
            if (theSymbol instanceof ISimpleLineSymbol) {
                ISimpleLineSymbol lineSymbol = (ISimpleLineSymbol) theSymbol;
                lineSymbol.setLineWidth(3);
            }
            theSymbol.setDescription(id + ": " + formatter.format(value));
            leg.addSymbol(id, theSymbol);
        }

        /*
         * if there are pipes that are dummy, make them grey
         */
        FeatureStore elRs = layer.getFeatureStore();
        FeatureSet set = null;
        DisposableIterator iterator = null;
        Color color = Color.lightGray;
        ISymbol theSymbol = symbolManager.createSymbol(layer.getShapeType(), color);
        if (theSymbol instanceof ISimpleLineSymbol) {
            ISimpleLineSymbol lineSymbol = (ISimpleLineSymbol) theSymbol;
            lineSymbol.setLineWidth(3);
        }
        theSymbol.setDescription("Virtual pipe");
        try {
            set = elRs.getFeatureSet();
            iterator = set.fastIterator();
            while( iterator.hasNext() ) {
                Feature feature = (Feature) iterator.next();
                Object pipeIdObject = feature.get(ID);
                if (pipeIdObject == null) {
                    continue;
                }
                String pipeId = (String) pipeIdObject;
                if (!idSet.contains(pipeId)) {
                    leg.addSymbol(pipeIdObject, theSymbol);
                }
            }
        } finally {
            if (iterator != null) {
                iterator.dispose();
            }
            if (set != null) {
                set.dispose();
            }
        }

        return leg;
    }

    public static VectorialUniqueValueLegend createPointLinkLegend( FLyrVect layer, List<ILinkResults> linksResults,
            ResultsLinkParameters linkVar, float[] linksMinMax, int size ) throws Exception {
        SymbolManager symbolManager = mapContextManager.getSymbolManager();
        VectorialUniqueValueLegend leg = (VectorialUniqueValueLegend) mapContextManager
                .createLegend(IVectorialUniqueValueLegend.LEGEND_NAME);

        leg.setClassifyingFieldNames(new String[]{ID});
        leg.setShapeType(layer.getShapeType());

        Color[] colorScheme = new Color[EpanetUtilities.rainbow.length];
        for( int i = 0; i < colorScheme.length; i++ ) {
            int[] rgb = EpanetUtilities.rainbow[i];
            colorScheme[i] = new Color(rgb[0], rgb[1], rgb[2]);
        }
        leg.setColorScheme(colorScheme);

        /*
         * first find min and max for the color ramp
         */
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;
        if (linksMinMax == null) {
            for( ILinkResults linkResult : linksResults ) {
                double value = getValue(linkVar, linkResult);
                min = (float) Math.min(value, min);
                max = (float) Math.max(value, max);
            }
        } else {
            min = linksMinMax[0];
            max = linksMinMax[1];
        }

        float delta = (max - min) / 5;
        double[] ramp = new double[6];
        for( int i = 0; i < ramp.length; i++ ) {
            ramp[i] = min + i * delta;
        }
        ColorArrayInterpolator colorInterpolator = new ColorArrayInterpolator(ramp, EpanetUtilities.rainbow);

        /*
         * then create color rules
         */
        DecimalFormat formatter = new DecimalFormat("0.00");
        TreeSet<String> idSet = new TreeSet<String>();
        for( ILinkResults linkResult : linksResults ) {
            double value = getValue(linkVar, linkResult);
            String id = linkResult.getId();
            idSet.add(id);
            int[] rgb = colorInterpolator.interpolate(value);
            Color color = new Color(rgb[0], rgb[1], rgb[2]);
            ISymbol theSymbol = symbolManager.createSymbol(layer.getShapeType(), color);
            if (theSymbol instanceof ISimpleMarkerSymbol) {
                ISimpleMarkerSymbol pointSymbol = (ISimpleMarkerSymbol) theSymbol;
                pointSymbol.setSize(size);
            }
            theSymbol.setDescription(id + ": " + formatter.format(value));
            leg.addSymbol(id, theSymbol);
        }

        return leg;
    }

    public static VectorialUniqueValueLegend createPointNodeLegend( FLyrVect layer, List<INodeResults> nodeResults,
            ResultsNodeParameters nodeVar, float[] nodeMinMax, int size ) throws Exception {
        SymbolManager symbolManager = mapContextManager.getSymbolManager();
        VectorialUniqueValueLegend leg = (VectorialUniqueValueLegend) mapContextManager
                .createLegend(IVectorialUniqueValueLegend.LEGEND_NAME);

        leg.setClassifyingFieldNames(new String[]{ID});
        leg.setShapeType(layer.getShapeType());

        Color[] colorScheme = new Color[EpanetUtilities.rainbow.length];
        for( int i = 0; i < colorScheme.length; i++ ) {
            int[] rgb = EpanetUtilities.rainbow[i];
            colorScheme[i] = new Color(rgb[0], rgb[1], rgb[2]);
        }
        leg.setColorScheme(colorScheme);

        /*
         * first find min and max for the color ramp
         */
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;
        if (nodeMinMax == null) {
            for( INodeResults nodeResult : nodeResults ) {
                double value = getValue(nodeVar, nodeResult);
                min = (float) Math.min(value, min);
                max = (float) Math.max(value, max);
            }
        } else {
            min = nodeMinMax[0];
            max = nodeMinMax[1];
        }

        float delta = (max - min) / 5;
        double[] ramp = new double[6];
        for( int i = 0; i < ramp.length; i++ ) {
            ramp[i] = min + i * delta;
        }
        ColorArrayInterpolator colorInterpolator = new ColorArrayInterpolator(ramp, EpanetUtilities.rainbow);

        /*
         * then create color rules
         */
        DecimalFormat formatter = new DecimalFormat("0.00");
        TreeSet<String> idSet = new TreeSet<String>();
        for( INodeResults nodeResult : nodeResults ) {
            double value = getValue(nodeVar, nodeResult);
            String id = nodeResult.getId();
            idSet.add(id);
            int[] rgb = colorInterpolator.interpolate(value);
            Color color = new Color(rgb[0], rgb[1], rgb[2]);
            ISymbol theSymbol = symbolManager.createSymbol(layer.getShapeType(), color);
            if (theSymbol instanceof ISimpleMarkerSymbol) {
                ISimpleMarkerSymbol pointSymbol = (ISimpleMarkerSymbol) theSymbol;
                pointSymbol.setSize(size);
            }
            theSymbol.setDescription(id + ": " + formatter.format(value));
            leg.addSymbol(id, theSymbol);
        }

        return leg;
    }

    private static double getValue( ResultsLinkParameters linkVar, ILinkResults linkResult ) {
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
        return value;
    }

    private static double getValue( ResultsNodeParameters nodeVar, INodeResults nodeResult ) {
        double value = -9999;
        switch( nodeVar ) {
        case DEMAND:
            value = nodeResult.getDemand();
            break;
        case HEAD:
            value = nodeResult.getHead();
            break;
        case PRESSURE:
            value = nodeResult.getPressure();
            break;
        default:
            throw new RuntimeException("Undefined variable.");
        }
        return value;
    }

}
