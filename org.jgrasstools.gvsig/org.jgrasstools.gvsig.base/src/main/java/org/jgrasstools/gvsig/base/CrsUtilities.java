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

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.mapcontext.MapContext;

/**
 * Utilities to handle CRS.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class CrsUtilities {

    /**
     * Reproject an envelope to the current map crs. 
     * 
     * @param envelope the original envelope.
     * @param fromCrs the crs to start from.
     * @param mapContext the mapcontext. If <code>null</code>, the current is picked.
     * @return the reprojected envelope.
     */
    public static Envelope reprojectToMapCrs( Envelope envelope, IProjection fromCrs, MapContext mapContext ) {
        if (mapContext == null) {
            mapContext = ProjectUtilities.getCurrentMapcontext();
        }
        IProjection mapCrs = mapContext.getViewPort().getProjection();
        ICoordTrans coordTrans = fromCrs.getCT(mapCrs);
        Geometry envelopeGeometry = envelope.getGeometry();
        envelopeGeometry.reProject(coordTrans);
        Envelope reprojectedEnvelope = envelopeGeometry.getEnvelope();
        return reprojectedEnvelope;
    }
}
