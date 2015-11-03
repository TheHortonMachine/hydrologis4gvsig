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

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.type.GeometryType;
import org.gvsig.tools.dispose.DisposableIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Conversion class to aid when converting between geotools and gvsig.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GtGvsigConversionUtilities {

    /**
     * Convert a gvSIG FeatureStore to geotools FeatureCollection.
     * 
     * @param store the store to convert.
     * @return the converted featurecollection.
     * @throws Exception
     */
    public static SimpleFeatureCollection toGtFeatureCollection( FeatureStore store ) throws Exception {
        FeatureType featureType = store.getDefaultFeatureType();

        WKBReader wkbReader = new WKBReader();
        WKTReader wktReader = new WKTReader();

        FeatureAttributeDescriptor[] attributeDescriptors = featureType.getAttributeDescriptors();

        Object crsObj = store.getDynValue("crs");
        // if (crsObj instanceof Crs) {
        // Crs new_name = (Crs) crsObj;
        //
        // }
        // org.gvsig.crs.Crs FIXME import the right proj lib
        CoordinateReferenceSystem crs = CRS.decode("EPSG:32632");

        SimpleFeatureTypeBuilder gtFeatureTypeBuilder = new SimpleFeatureTypeBuilder();
        gtFeatureTypeBuilder.setName(store.getName());
        gtFeatureTypeBuilder.setCRS(crs);

        int attributesCount = 0;
        int geomIndex = -1;

        for( FeatureAttributeDescriptor attributeDescriptor : attributeDescriptors ) {
            String name = attributeDescriptor.getName();
            Class< ? > clazz = attributeDescriptor.getDataType().getDefaultClass();
            // Class< ? > clazz = attributeDescriptor.getClassOfValue();

            GeometryType geomType = null;
            try {
                geomType = attributeDescriptor.getGeomType();
            } catch (Exception e) {
                // ignore
            }
            if (geomType != null) {
                name = "the_geom";
                geomIndex = attributesCount;
                int type = geomType.getType();
                switch( type ) {
                case Geometry.TYPES.POINT:
                    clazz = Point.class;
                    break;
                case Geometry.TYPES.MULTIPOINT:
                    clazz = MultiPoint.class;
                    break;
                case Geometry.TYPES.LINE:
                    clazz = LineString.class;
                    break;
                case Geometry.TYPES.MULTICURVE:
                    clazz = MultiLineString.class;
                    break;
                case Geometry.TYPES.POLYGON:
                    clazz = Polygon.class;
                    break;
                case Geometry.TYPES.MULTISURFACE:
                    clazz = MultiPolygon.class;
                    break;
                default:
                    throw new IllegalArgumentException("Can't recognize geomatry type: " + type);
                }
            }
            gtFeatureTypeBuilder.add(name, clazz);
            attributesCount++;
        }

        DefaultFeatureCollection gtFeatureCollection = new DefaultFeatureCollection();

        SimpleFeatureType gtFeatureType = gtFeatureTypeBuilder.buildFeatureType();
        SimpleFeatureBuilder gtFeatureBuilder = new SimpleFeatureBuilder(gtFeatureType);

        FeatureSet featureSet = store.getFeatureSet();
        DisposableIterator featureIterator = featureSet.fastIterator();
        while( featureIterator.hasNext() ) {
            Feature feature = (Feature) featureIterator.next();

            List<Object> gtAttributesList = new ArrayList<Object>();

            for( int i = 0; i < attributesCount; i++ ) {
                Object object = feature.get(i);
                if (i != geomIndex) {
                    gtAttributesList.add(object);
                } else {
                    // convert geometry
                    // byte[] wkb = feature.getDefaultGeometry().convertToWKB();
                    String wkt = feature.getDefaultGeometry().convertToWKT();
                    // com.vividsolutions.jts.geom.Geometry geometry = wkbReader.read(wkb);
                    com.vividsolutions.jts.geom.Geometry geometry2 = wktReader.read(wkt);
                    gtAttributesList.add(geometry2);
                }
            }

            gtFeatureBuilder.addAll(gtAttributesList);
            SimpleFeature gtFeature = gtFeatureBuilder.buildFeature(null);
            gtFeatureCollection.add(gtFeature);
        }

        return gtFeatureCollection;
    }

}
