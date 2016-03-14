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

import org.gvsig.fmap.geom.Geometry.SUBTYPES;
import org.gvsig.fmap.geom.Geometry.TYPES;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.exception.CreateGeometryException;
import org.gvsig.fmap.geom.primitive.Curve;
import org.gvsig.fmap.geom.primitive.Point;
import org.gvsig.fmap.geom.primitive.Surface;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Geometry utils methods.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeometryUtilities {
    private static GeometryManager geometryManager = GeometryLocator.getGeometryManager();

    public static Point createPoint2D( double x, double y ) throws CreateGeometryException {
        Point point = geometryManager.createPoint(x, y, SUBTYPES.GEOM2D);
        return point;
    }

    public static Point createPoint3D( double x, double y, double z ) throws CreateGeometryException {
        Point point = geometryManager.createPoint(x, y, SUBTYPES.GEOM3D);
        point.setCoordinateAt(2, z);
        return point;
    }

    public static Curve createLine2D( Coordinate[] coords ) throws CreateGeometryException {
        Curve curve = (Curve) geometryManager.create(TYPES.CURVE, SUBTYPES.GEOM2D);
        for( Coordinate coordinate : coords ) {
            curve.addVertex(coordinate.x, coordinate.y);
        }
        return curve;
    }

    public static Curve createLine3D( Coordinate[] coords ) throws CreateGeometryException {
        Curve curve = (Curve) geometryManager.create(TYPES.CURVE, SUBTYPES.GEOM3D);
        for( Coordinate coordinate : coords ) {
            curve.addVertex(coordinate.x, coordinate.y, coordinate.z);
        }
        return curve;
    }

    public static Surface createPolygon2D( Coordinate[] coords ) throws CreateGeometryException {
        Surface surface = (Surface) geometryManager.create(TYPES.SURFACE, SUBTYPES.GEOM2D);
        for( Coordinate coordinate : coords ) {
            surface.addVertex(coordinate.x, coordinate.y);
        }
        return surface;
    }

    public static Surface createPolygon3D( Coordinate[] coords ) throws CreateGeometryException {
        Surface surface = (Surface) geometryManager.create(TYPES.SURFACE, SUBTYPES.GEOM3D);
        for( Coordinate coordinate : coords ) {
            surface.addVertex(coordinate.x, coordinate.y, coordinate.z);
        }
        return surface;
    }

}
