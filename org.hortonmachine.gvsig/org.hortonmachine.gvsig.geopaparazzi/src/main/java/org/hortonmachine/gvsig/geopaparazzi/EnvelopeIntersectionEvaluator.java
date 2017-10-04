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
package org.hortonmachine.gvsig.geopaparazzi;

import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.operation.GeometryOperationException;
import org.gvsig.fmap.geom.operation.GeometryOperationNotSupportedException;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.geom.primitive.Point;
import org.gvsig.tools.evaluator.AbstractEvaluator;
import org.gvsig.tools.evaluator.EvaluatorData;
import org.gvsig.tools.evaluator.EvaluatorException;

public class EnvelopeIntersectionEvaluator extends AbstractEvaluator {

    private String op2attrname;
    private String where;
    private Envelope rect;
    private GeometryManager geometryManager;

    public EnvelopeIntersectionEvaluator( String op1attrname, Envelope rect )
            throws GeometryOperationNotSupportedException, GeometryOperationException {
        this.op2attrname = op1attrname;
        this.rect = rect;
        this.where = "";

        geometryManager = GeometryLocator.getGeometryManager();
    }

    public String getName() {
        return "envelopesIntersect";
    }

    public Object evaluate( EvaluatorData data ) throws EvaluatorException {
        Point geom = (Point) data.getDataValue(this.op2attrname);
        double x = geom.getX();
        double y = geom.getY();
        try {
            Envelope envelope = geometryManager.createEnvelope(x, y, x, y, Geometry.SUBTYPES.GEOM2D);
            boolean intersects = rect.intersects(envelope);
            return new Boolean(intersects);
        } catch (Exception e) {
            e.printStackTrace();
            return new Boolean(false);
        }
    }

    public String getCQL() {
        return this.where;
    }

}
