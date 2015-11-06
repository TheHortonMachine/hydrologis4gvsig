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
package org.jgrasstools.gvsig.geopaparazzi;

import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.generalpath.primitive.Envelope2D;
import org.gvsig.fmap.geom.generalpath.primitive.point.Point2D;
import org.gvsig.fmap.geom.operation.GeometryOperationException;
import org.gvsig.fmap.geom.operation.GeometryOperationNotSupportedException;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.tools.evaluator.AbstractEvaluator;
import org.gvsig.tools.evaluator.EvaluatorData;
import org.gvsig.tools.evaluator.EvaluatorException;

public class EnvelopeIntersectionEvaluator extends AbstractEvaluator {

    private String op2attrname;
    private String where;
    private Envelope rect;

    public EnvelopeIntersectionEvaluator( String op1attrname, Envelope rect )
            throws GeometryOperationNotSupportedException, GeometryOperationException {
        this.op2attrname = op1attrname;
        this.rect = rect;
        this.where = "";
    }

    public String getName() {
        return "envelopesIntersect";
    }

    public Object evaluate( EvaluatorData data ) throws EvaluatorException {
        Point2D geom = (Point2D) data.getDataValue(this.op2attrname);
        try {
            Envelope envelope = new Envelope2D(geom, geom);
            boolean intersects = rect.intersects(envelope);
            return new Boolean(intersects);
        } catch (Exception e) {
            throw new EvaluatorException(e);
        }
    }

    public String getCQL() {
        return this.where;
    }

}
