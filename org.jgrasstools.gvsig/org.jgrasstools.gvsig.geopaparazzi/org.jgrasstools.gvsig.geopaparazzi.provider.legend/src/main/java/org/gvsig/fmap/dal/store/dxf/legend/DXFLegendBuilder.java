/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 */

/*
* AUTHORS (In addition to CIT):
* 2008 IVER T.I. S.A.   {{Task}}
*/

package org.gvsig.fmap.dal.store.dxf.legend;

import java.awt.Color;

import org.gvsig.dxf.px.dxf.AcadColor;
import org.gvsig.fmap.dal.DataTypes;
import org.gvsig.fmap.dal.feature.spi.FeatureProvider;
import org.gvsig.fmap.dal.feature.spi.FeatureStoreProvider;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorialUniqueValueLegend;
import org.gvsig.fmap.mapcontext.rendering.symbols.ISymbol;
import org.gvsig.symbology.SymbologyLocator;
import org.gvsig.symbology.fmap.mapcontext.rendering.legend.styling.IAttrInTableLabelingStrategy;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.fill.IFillSymbol;
import org.gvsig.symbology.fmap.mapcontext.rendering.symbol.line.ILineSymbol;
import org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi.GPSPStoreProvider;
import org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi.LegendBuilder;

public class DXFLegendBuilder implements LegendBuilder {

	private MapContextManager mapContextManager = MapContextLocator
			.getMapContextManager();

	private IVectorialUniqueValueLegend defaultLegend = null;
	private IAttrInTableLabelingStrategy labelingStragegy = null;

	public void begin() {
		// Nothing to do
	}

	public void end() {
		defaultLegend.useDefaultSymbol(true);
	}

	public Object getLegend() {
		return defaultLegend;
	}

	public LegendBuilder initialize(FeatureStoreProvider store) {
		defaultLegend = (IVectorialUniqueValueLegend) mapContextManager
				.createLegend(IVectorialUniqueValueLegend.LEGEND_NAME);
		defaultLegend.setShapeType(Geometry.TYPES.GEOMETRY);
		defaultLegend
				.setClassifyingFieldNames(new String[] { GPSPStoreProvider.NAME_FIELD_COLOR });
		defaultLegend.setClassifyingFieldTypes(new int[] { DataTypes.INT });

		ISymbol myDefaultSymbol =
				mapContextManager.getSymbolManager().createSymbol(
				Geometry.TYPES.GEOMETRY, Color.BLACK);

		defaultLegend.setDefaultSymbol(myDefaultSymbol);
		defaultLegend.useDefaultSymbol(false);

		labelingStragegy = SymbologyLocator.getSymbologyManager().createAttrInTableLabelingStrategy();
		labelingStragegy.setTextField(GPSPStoreProvider.NAME_FIELD_TEXT);
		labelingStragegy
				.setRotationField(GPSPStoreProvider.NAME_FIELD_ROTATIONTEXT);
		labelingStragegy.setHeightField(GPSPStoreProvider.NAME_FIELD_HEIGHTTEXT);
		labelingStragegy.setUnit(1); // MapContext.NAMES[1] (meters)
		return this;
	}

	public void process(FeatureProvider feature) {
		Integer clave = (Integer) feature.get("Color");
		if (clave == null) {
			return;
		}

		if (defaultLegend.getSymbolByValue(clave) == null) {
			ISymbol theSymbol;
			Color color = null;
			try {
				color = AcadColor.getColor(clave.intValue());
			} catch (ArrayIndexOutOfBoundsException e) {
				color = AcadColor.getColor(255);
			}
			// jaume, moved to ISymbol
			theSymbol =
					mapContextManager.getSymbolManager().createSymbol(
							Geometry.TYPES.GEOMETRY,
					color);
			theSymbol.setDescription(clave.toString());
			// Asigna los colores de Autocad a los
			// bordes de los pol�gonos y pone el relleno transparente.
			if (theSymbol instanceof IFillSymbol) {
				((IFillSymbol) theSymbol).getOutline().setLineColor(color);
				Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
				((IFillSymbol) theSymbol).setFillColor(fillColor);
			}
			// Asigna los colores de Autocad a las l�neas
			if (theSymbol instanceof ILineSymbol) {
				((ILineSymbol) theSymbol).setLineColor(color);
			}
			if (theSymbol != null) {
				defaultLegend.addSymbol(clave, theSymbol);
			}
		}
	}

	public Object getLabeling() {
		return labelingStragegy;
	}


}
